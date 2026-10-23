package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Estadisticas;

import java.util.Comparator;
import java.util.List;

/** Panel del administrador: alumnos, asignaturas por titulación y ocupación de cada asignatura. */
public class ServicioEstadisticas {

    private final BaseDatos db;

    public ServicioEstadisticas(BaseDatos db) {
        this.db = db;
    }

    public Estadisticas calcular() {
        return db.transaccion(c -> new Estadisticas(
                Jdbc.entero(c, "SELECT COUNT(*) FROM alumno"),
                Jdbc.entero(c, "SELECT COUNT(*) FROM asignatura"),
                Jdbc.entero(c, "SELECT COUNT(*) FROM matricula"),
                Jdbc.lista(c, """
                        SELECT t.nombre, COUNT(a.id) AS n
                        FROM titulacion t LEFT JOIN asignatura a ON a.id_titulacion = t.id
                        GROUP BY t.id, t.nombre ORDER BY n DESC, t.nombre""",
                        rs -> new Estadisticas.Fila(rs.getString("nombre"), rs.getInt("n"))),
                ordenarPorOcupacion(Jdbc.lista(c, ServicioAsignaturas.SELECT, ServicioAsignaturas::mapear))));
    }

    private static List<Asignatura> ordenarPorOcupacion(List<Asignatura> asignaturas) {
        asignaturas.sort(Comparator.comparingDouble((Asignatura a) -> (double) a.getMatriculados() / a.getCapacidad())
                .reversed()
                .thenComparing(Asignatura::getNombre));
        return asignaturas;
    }
}
