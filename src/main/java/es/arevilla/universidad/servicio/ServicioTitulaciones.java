package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Titulacion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicioTitulaciones {

    private static final String SELECT = """
            SELECT t.id, t.nombre, t.descripcion,
                   (SELECT COUNT(*) FROM asignatura a WHERE a.id_titulacion = t.id) AS num_asignaturas
            FROM titulacion t
            """;

    private final BaseDatos db;

    public ServicioTitulaciones(BaseDatos db) {
        this.db = db;
    }

    public List<Titulacion> listar() {
        return db.transaccion(c -> Jdbc.lista(c, SELECT + " ORDER BY t.nombre", ServicioTitulaciones::mapear));
    }

    public Titulacion buscar(int id) {
        return db.transaccion(c -> Jdbc.uno(c, SELECT + " WHERE t.id = ?", ServicioTitulaciones::mapear, id))
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe la titulación " + id));
    }

    public Titulacion crear(Titulacion datos) {
        Titulacion t = validar(datos);
        return db.transaccion(c -> {
            comprobarNombreLibre(c, t.getNombre(), 0);
            t.setId(Jdbc.insertar(c, "INSERT INTO titulacion (nombre, descripcion) VALUES (?, ?)",
                    t.getNombre(), t.getDescripcion()));
            return t;
        });
    }

    public Titulacion actualizar(int id, Titulacion datos) {
        Titulacion t = validar(datos);
        t.setId(id);
        return db.transaccion(c -> {
            comprobarNombreLibre(c, t.getNombre(), id);
            if (Jdbc.actualizar(c, "UPDATE titulacion SET nombre = ?, descripcion = ? WHERE id = ?",
                    t.getNombre(), t.getDescripcion(), id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe la titulación " + id);
            }
            return t;
        });
    }

    /** No se puede borrar una titulación con asignaturas: se quedarían huérfanas. */
    public void eliminar(int id) {
        db.transaccion(c -> {
            int asignaturas = Jdbc.entero(c, "SELECT COUNT(*) FROM asignatura WHERE id_titulacion = ?", id);
            if (asignaturas > 0) {
                throw ErrorNegocio.conflicto("No se puede borrar la titulación: tiene " + asignaturas
                        + " asignatura(s). Bórralas o cámbialas de titulación antes.");
            }
            if (Jdbc.actualizar(c, "DELETE FROM titulacion WHERE id = ?", id) == 0) {
                throw ErrorNegocio.noEncontrado("No existe la titulación " + id);
            }
            return null;
        });
    }

    private static Titulacion validar(Titulacion datos) {
        if (datos == null) throw ErrorNegocio.validacion("Faltan los datos de la titulación");
        return new Titulacion(0,
                Validar.obligatorio(datos.getNombre(), "nombre", 120),
                Validar.opcional(datos.getDescripcion(), "descripción", 500));
    }

    private static void comprobarNombreLibre(java.sql.Connection c, String nombre, int idActual)
            throws SQLException {
        if (Jdbc.entero(c, "SELECT COUNT(*) FROM titulacion WHERE LOWER(nombre) = LOWER(?) AND id <> ?",
                nombre, idActual) > 0) {
            throw ErrorNegocio.conflicto("Ya existe una titulación llamada «" + nombre + "»");
        }
    }

    private static Titulacion mapear(ResultSet rs) throws SQLException {
        Titulacion t = new Titulacion(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
        t.setNumAsignaturas(rs.getInt("num_asignaturas"));
        return t;
    }
}
