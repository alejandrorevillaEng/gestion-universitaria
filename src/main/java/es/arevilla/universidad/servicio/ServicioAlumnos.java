package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Alumno;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicioAlumnos {

    static final String SELECT = """
            SELECT al.id, al.nombre, al.apellidos, al.email,
                   (SELECT COUNT(*) FROM matricula m WHERE m.id_alumno = al.id) AS num_asignaturas
            FROM alumno al
            """;

    private final BaseDatos db;

    public ServicioAlumnos(BaseDatos db) {
        this.db = db;
    }

    public List<Alumno> listar() {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " ORDER BY al.apellidos, al.nombre",
                ServicioAlumnos::mapear));
    }

    public Alumno buscar(int id) {
        return db.transaccion(c -> Jdbc.uno(c, SELECT + " WHERE al.id = ?", ServicioAlumnos::mapear, id))
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el alumno " + id));
    }

    public Alumno crear(Alumno datos) {
        Alumno a = validar(datos);
        return db.transaccion(c -> {
            comprobarEmailLibre(c, a.getEmail(), 0);
            a.setId(Jdbc.insertar(c, "INSERT INTO alumno (nombre, apellidos, email) VALUES (?, ?, ?)",
                    a.getNombre(), a.getApellidos(), a.getEmail()));
            return a;
        });
    }

    public Alumno actualizar(int id, Alumno datos) {
        Alumno a = validar(datos);
        a.setId(id);
        return db.transaccion(c -> {
            comprobarEmailLibre(c, a.getEmail(), id);
            if (Jdbc.actualizar(c, "UPDATE alumno SET nombre = ?, apellidos = ?, email = ? WHERE id = ?",
                    a.getNombre(), a.getApellidos(), a.getEmail(), id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe el alumno " + id);
            }
            return a;
        });
    }

    /** Al borrar un alumno se borran también sus matrículas (ON DELETE CASCADE). */
    public void eliminar(int id) {
        db.transaccion(c -> {
            if (Jdbc.actualizar(c, "DELETE FROM alumno WHERE id = ?", id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe el alumno " + id);
            }
            return null;
        });
    }

    private static Alumno validar(Alumno datos) {
        if (datos == null) throw ErrorNegocio.validacion("Faltan los datos del alumno");
        return new Alumno(0,
                Validar.obligatorio(datos.getNombre(), "nombre", 80),
                Validar.obligatorio(datos.getApellidos(), "apellidos", 120),
                Validar.email(datos.getEmail()));
    }

    private static void comprobarEmailLibre(Connection c, String email, int idActual) throws SQLException {
        if (Jdbc.entero(c, "SELECT COUNT(*) FROM alumno WHERE email = ? AND id <> ?", email, idActual) > 0) {
            throw ErrorNegocio.conflicto("Ya hay un alumno con el email " + email);
        }
    }

    static Alumno mapear(ResultSet rs) throws SQLException {
        Alumno a = new Alumno(rs.getInt("id"), rs.getString("nombre"), rs.getString("apellidos"),
                rs.getString("email"));
        a.setNumAsignaturas(rs.getInt("num_asignaturas"));
        return a;
    }
}
