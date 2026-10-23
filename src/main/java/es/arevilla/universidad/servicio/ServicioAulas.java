package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Aula;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Aulas y su relación N:M con las asignaturas. */
public class ServicioAulas {

    static final String SELECT = """
            SELECT au.id, au.nombre, au.edificio, au.capacidad,
                   (SELECT COUNT(*) FROM asignatura_aula aa WHERE aa.id_aula = au.id) AS num_asignaturas
            FROM aula au
            """;

    private final BaseDatos db;

    public ServicioAulas(BaseDatos db) {
        this.db = db;
    }

    public List<Aula> listar() {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " ORDER BY au.nombre", ServicioAulas::mapear));
    }

    public Aula buscar(int id) {
        return db.transaccion(c -> buscar(c, id));
    }

    static Aula buscar(Connection c, int id) throws SQLException {
        return Jdbc.uno(c, SELECT + " WHERE au.id = ?", ServicioAulas::mapear, id)
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el aula " + id));
    }

    public Aula crear(Aula datos) {
        Aula a = validar(datos);
        return db.transaccion(c -> {
            comprobarNombreLibre(c, a.getNombre(), 0);
            int id = Jdbc.insertar(c, "INSERT INTO aula (nombre, edificio, capacidad) VALUES (?, ?, ?)",
                    a.getNombre(), a.getEdificio(), a.getCapacidad());
            return buscar(c, id);
        });
    }

    public Aula actualizar(int id, Aula datos) {
        Aula a = validar(datos);
        return db.transaccion(c -> {
            buscar(c, id);
            comprobarNombreLibre(c, a.getNombre(), id);
            int mayor = Jdbc.entero(c, """
                    SELECT COALESCE(MAX(asig.capacidad), 0) FROM asignatura asig
                    JOIN asignatura_aula aa ON aa.id_asignatura = asig.id WHERE aa.id_aula = ?""", id);
            if (a.getCapacidad() < mayor) {
                throw ErrorNegocio.conflicto("El aula acoge una asignatura de " + mayor
                        + " plazas: no puede tener menos capacidad");
            }
            Jdbc.actualizar(c, "UPDATE aula SET nombre = ?, edificio = ?, capacidad = ? WHERE id = ?",
                    a.getNombre(), a.getEdificio(), a.getCapacidad(), id);
            return buscar(c, id);
        });
    }

    public void eliminar(int id) {
        db.transaccion(c -> {
            Aula a = buscar(c, id);
            if (a.getNumAsignaturas() > 0) {
                throw ErrorNegocio.conflicto("No se puede borrar el aula " + a.getNombre() + ": tiene "
                        + a.getNumAsignaturas() + " asignatura(s). Quítaselas antes.");
            }
            Jdbc.actualizar(c, "DELETE FROM aula WHERE id = ?", id);
            return null;
        });
    }

    /** Un aula solo sirve si caben todos los alumnos que admite la asignatura. */
    public void asignar(int idAsignatura, int idAula) {
        db.transaccion(c -> {
            Asignatura asig = ServicioAsignaturas.buscar(c, idAsignatura);
            Aula aula = buscar(c, idAula);
            if (aula.getCapacidad() < asig.getCapacidad()) {
                throw ErrorNegocio.conflicto("El aula " + aula.getNombre() + " tiene " + aula.getCapacidad()
                        + " plazas y la asignatura admite " + asig.getCapacidad());
            }
            if (Jdbc.entero(c, "SELECT COUNT(*) FROM asignatura_aula WHERE id_asignatura = ? AND id_aula = ?",
                    idAsignatura, idAula) > 0) {
                throw ErrorNegocio.conflicto("La asignatura ya tiene asignada el aula " + aula.getNombre());
            }
            Jdbc.actualizar(c, "INSERT INTO asignatura_aula (id_asignatura, id_aula) VALUES (?, ?)", idAsignatura, idAula);
            return null;
        });
    }

    public void quitar(int idAsignatura, int idAula) {
        db.transaccion(c -> {
            if (Jdbc.actualizar(c, "DELETE FROM asignatura_aula WHERE id_asignatura = ? AND id_aula = ?",
                    idAsignatura, idAula) == 0) {
                throw ErrorNegocio.noEncontrado("La asignatura no tiene asignada esa aula");
            }
            return null;
        });
    }

    public List<Aula> aulasDe(int idAsignatura) {
        return db.transaccion(c -> {
            ServicioAsignaturas.buscar(c, idAsignatura);
            return Jdbc.lista(c, SELECT + """
                     WHERE au.id IN (SELECT id_aula FROM asignatura_aula WHERE id_asignatura = ?)
                     ORDER BY au.nombre""", ServicioAulas::mapear, idAsignatura);
        });
    }

    /** Aulas libres para la asignatura con plazas suficientes (para el desplegable). */
    public List<Aula> aulasDisponiblesPara(int idAsignatura) {
        return db.transaccion(c -> {
            Asignatura asig = ServicioAsignaturas.buscar(c, idAsignatura);
            return Jdbc.lista(c, SELECT + """
                     WHERE au.capacidad >= ?
                       AND au.id NOT IN (SELECT id_aula FROM asignatura_aula WHERE id_asignatura = ?)
                     ORDER BY au.nombre""", ServicioAulas::mapear, asig.getCapacidad(), idAsignatura);
        });
    }

    private static Aula validar(Aula datos) {
        if (datos == null) throw ErrorNegocio.validacion("Faltan los datos del aula");
        return new Aula(0,
                Validar.obligatorio(datos.getNombre(), "nombre", 40),
                Validar.opcional(datos.getEdificio(), "edificio", 80),
                Validar.rango(datos.getCapacidad(), "capacidad", 1, 1000));
    }

    private static void comprobarNombreLibre(Connection c, String nombre, int idActual) throws SQLException {
        if (Jdbc.entero(c, "SELECT COUNT(*) FROM aula WHERE LOWER(nombre) = LOWER(?) AND id <> ?", nombre, idActual) > 0) {
            throw ErrorNegocio.conflicto("Ya existe un aula llamada " + nombre);
        }
    }

    static Aula mapear(ResultSet rs) throws SQLException {
        Aula a = new Aula(rs.getInt("id"), rs.getString("nombre"), rs.getString("edificio"), rs.getInt("capacidad"));
        a.setNumAsignaturas(rs.getInt("num_asignaturas"));
        return a;
    }
}
