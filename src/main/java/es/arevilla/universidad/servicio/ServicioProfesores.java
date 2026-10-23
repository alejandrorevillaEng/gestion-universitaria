package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Profesor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicioProfesores {

    private static final String SELECT = """
            SELECT p.id, p.nombre, p.especialidad,
                   (SELECT COUNT(*) FROM asignatura a WHERE a.id_profesor = p.id) AS num_asignaturas
            FROM profesor p
            """;

    private final BaseDatos db;

    public ServicioProfesores(BaseDatos db) {
        this.db = db;
    }

    public List<Profesor> listar() {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " ORDER BY p.nombre", ServicioProfesores::mapear));
    }

    public Profesor buscar(int id) {
        return db.transaccion(c -> Jdbc.uno(c, SELECT + " WHERE p.id = ?", ServicioProfesores::mapear, id))
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el profesor " + id));
    }

    public Profesor crear(Profesor datos) {
        Profesor p = validar(datos);
        return db.transaccion(c -> {
            p.setId(Jdbc.insertar(c, "INSERT INTO profesor (nombre, especialidad) VALUES (?, ?)",
                    p.getNombre(), p.getEspecialidad()));
            return p;
        });
    }

    public Profesor actualizar(int id, Profesor datos) {
        Profesor p = validar(datos);
        p.setId(id);
        return db.transaccion(c -> {
            if (Jdbc.actualizar(c, "UPDATE profesor SET nombre = ?, especialidad = ? WHERE id = ?",
                    p.getNombre(), p.getEspecialidad(), id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe el profesor " + id);
            }
            return p;
        });
    }

    /** No se borra un profesor que imparte asignaturas: primero hay que reasignarlas. */
    public void eliminar(int id) {
        db.transaccion(c -> {
            int asignaturas = Jdbc.entero(c, "SELECT COUNT(*) FROM asignatura WHERE id_profesor = ?", id);
            if (asignaturas > 0) {
                throw ErrorNegocio.conflicto("No se puede borrar el profesor: imparte " + asignaturas
                        + " asignatura(s). Quítaselas antes.");
            }
            if (Jdbc.actualizar(c, "DELETE FROM profesor WHERE id = ?", id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe el profesor " + id);
            }
            return null;
        });
    }

    private static Profesor validar(Profesor datos) {
        if (datos == null) throw ErrorNegocio.validacion("Faltan los datos del profesor");
        return new Profesor(0,
                Validar.obligatorio(datos.getNombre(), "nombre", 120),
                Validar.opcional(datos.getEspecialidad(), "especialidad", 120));
    }

    private static Profesor mapear(ResultSet rs) throws SQLException {
        Profesor p = new Profesor(rs.getInt("id"), rs.getString("nombre"), rs.getString("especialidad"));
        p.setNumAsignaturas(rs.getInt("num_asignaturas"));
        return p;
    }
}
