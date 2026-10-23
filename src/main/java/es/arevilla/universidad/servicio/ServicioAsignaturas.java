package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Asignatura;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicioAsignaturas {

    static final String SELECT = """
            SELECT a.id, a.nombre, a.creditos, a.capacidad, a.id_titulacion, a.id_profesor,
                   t.nombre AS nombre_titulacion, p.nombre AS nombre_profesor,
                   (SELECT COUNT(*) FROM matricula m WHERE m.id_asignatura = a.id) AS matriculados
            FROM asignatura a
            JOIN titulacion t ON t.id = a.id_titulacion
            LEFT JOIN profesor p ON p.id = a.id_profesor
            """;

    private final BaseDatos db;

    public ServicioAsignaturas(BaseDatos db) {
        this.db = db;
    }

    public List<Asignatura> listar() {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " ORDER BY t.nombre, a.nombre",
                ServicioAsignaturas::mapear));
    }

    public List<Asignatura> listarPorTitulacion(int idTitulacion) {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " WHERE a.id_titulacion = ? ORDER BY a.nombre",
                ServicioAsignaturas::mapear, idTitulacion));
    }

    public Asignatura buscar(int id) {
        return db.transaccion(c -> buscar(c, id));
    }

    static Asignatura buscar(Connection c, int id) throws SQLException {
        return Jdbc.uno(c, SELECT + " WHERE a.id = ?", ServicioAsignaturas::mapear, id)
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe la asignatura " + id));
    }

    public Asignatura crear(Asignatura datos) {
        Asignatura a = validar(datos);
        return db.transaccion(c -> {
            comprobarReferencias(c, a);
            comprobarNombreLibre(c, a, 0);
            int id = Jdbc.insertar(c, """
                    INSERT INTO asignatura (nombre, creditos, capacidad, id_titulacion, id_profesor)
                    VALUES (?, ?, ?, ?, ?)""",
                    a.getNombre(), a.getCreditos(), a.getCapacidad(), a.getIdTitulacion(), a.getIdProfesor());
            return buscar(c, id);
        });
    }

    public Asignatura actualizar(int id, Asignatura datos) {
        Asignatura a = validar(datos);
        return db.transaccion(c -> {
            Asignatura actual = buscar(c, id);
            comprobarReferencias(c, a);
            a.setId(id);
            comprobarNombreLibre(c, a, id);
            if (a.getCapacidad() < actual.getMatriculados()) {
                throw ErrorNegocio.conflicto("La capacidad no puede ser menor que los " + actual.getMatriculados()
                        + " alumnos ya matriculados");
            }
            Jdbc.actualizar(c, """
                    UPDATE asignatura SET nombre = ?, creditos = ?, capacidad = ?, id_titulacion = ?, id_profesor = ?
                    WHERE id = ?""",
                    a.getNombre(), a.getCreditos(), a.getCapacidad(), a.getIdTitulacion(), a.getIdProfesor(), id);
            return buscar(c, id);
        });
    }

    /** Requisito del enunciado: si la asignatura tiene alumnos matriculados, se avisa y no se borra. */
    public void eliminar(int id) {
        db.transaccion(c -> {
            Asignatura a = buscar(c, id);
            if (a.getMatriculados() > 0) {
                throw ErrorNegocio.conflicto("No se puede borrar «" + a.getNombre() + "»: tiene "
                        + a.getMatriculados() + " alumno(s) matriculado(s)");
            }
            Jdbc.actualizar(c, "DELETE FROM asignatura WHERE id = ?", id);
            return null;
        });
    }

    /** Asigna un profesor a la asignatura, o la deja sin profesor si idProfesor es null. */
    public Asignatura asignarProfesor(int idAsignatura, Integer idProfesor) {
        return db.transaccion(c -> {
            buscar(c, idAsignatura);
            if (idProfesor != null && !existe(c, "profesor", idProfesor)) {
                throw ErrorNegocio.noEncontrado("No existe el profesor " + idProfesor);
            }
            Jdbc.actualizar(c, "UPDATE asignatura SET id_profesor = ? WHERE id = ?", idProfesor, idAsignatura);
            return buscar(c, idAsignatura);
        });
    }

    private static Asignatura validar(Asignatura datos) {
        if (datos == null) throw ErrorNegocio.validacion("Faltan los datos de la asignatura");
        Asignatura a = new Asignatura(0,
                Validar.obligatorio(datos.getNombre(), "nombre", 120),
                Validar.rango(datos.getCreditos(), "créditos", 1, 18),
                Validar.rango(datos.getCapacidad(), "capacidad", 1, 500),
                datos.getIdTitulacion(),
                datos.getIdProfesor() != null && datos.getIdProfesor() == 0 ? null : datos.getIdProfesor());
        if (a.getIdTitulacion() <= 0) throw ErrorNegocio.validacion("Elige una titulación");
        return a;
    }

    private static void comprobarReferencias(Connection c, Asignatura a) throws SQLException {
        if (!existe(c, "titulacion", a.getIdTitulacion())) {
            throw ErrorNegocio.validacion("No existe la titulación " + a.getIdTitulacion());
        }
        if (a.getIdProfesor() != null && !existe(c, "profesor", a.getIdProfesor())) {
            throw ErrorNegocio.validacion("No existe el profesor " + a.getIdProfesor());
        }
    }

    private static void comprobarNombreLibre(Connection c, Asignatura a, int idActual) throws SQLException {
        if (Jdbc.entero(c, """
                SELECT COUNT(*) FROM asignatura
                WHERE LOWER(nombre) = LOWER(?) AND id_titulacion = ? AND id <> ?""",
                a.getNombre(), a.getIdTitulacion(), idActual) > 0) {
            throw ErrorNegocio.conflicto("Ya hay una asignatura «" + a.getNombre() + "» en esa titulación");
        }
    }

    private static boolean existe(Connection c, String tabla, int id) throws SQLException {
        String sql = switch (tabla) {
            case "titulacion" -> "SELECT COUNT(*) FROM titulacion WHERE id = ?";
            case "profesor" -> "SELECT COUNT(*) FROM profesor WHERE id = ?";
            default -> throw new IllegalArgumentException("Tabla no prevista: " + tabla);
        };
        return Jdbc.entero(c, sql, id) > 0;
    }

    static Asignatura mapear(ResultSet rs) throws SQLException {
        Asignatura a = new Asignatura(rs.getInt("id"), rs.getString("nombre"), rs.getInt("creditos"),
                rs.getInt("capacidad"), rs.getInt("id_titulacion"), Jdbc.enteroONulo(rs, "id_profesor"));
        a.setNombreTitulacion(rs.getString("nombre_titulacion"));
        a.setNombreProfesor(rs.getString("nombre_profesor"));
        a.setMatriculados(rs.getInt("matriculados"));
        return a;
    }
}
