package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.seguridad.LimiteIntentos;

/**
 * Punto de acceso a todos los servicios. Se crea una vez al arrancar y se
 * guarda en el ServletContext, del que lo leen los servlets y la API REST:
 * así la web y la API comparten exactamente los mismos datos y reglas.
 */
public record Servicios(BaseDatos db,
                        ServicioUsuarios usuarios,
                        ServicioTitulaciones titulaciones,
                        ServicioProfesores profesores,
                        ServicioAsignaturas asignaturas,
                        ServicioAlumnos alumnos,
                        ServicioMatriculas matriculas,
                        ServicioAulas aulas,
                        ServicioEstadisticas estadisticas) implements AutoCloseable {

    public static final String ATRIBUTO = Servicios.class.getName();

    public static Servicios crear(BaseDatos db, LimiteIntentos limite) {
        return new Servicios(db,
                new ServicioUsuarios(db, limite),
                new ServicioTitulaciones(db),
                new ServicioProfesores(db),
                new ServicioAsignaturas(db),
                new ServicioAlumnos(db),
                new ServicioMatriculas(db),
                new ServicioAulas(db),
                new ServicioEstadisticas(db));
    }

    /** Aplica las migraciones y, si la base de datos está vacía, carga los datos de ejemplo. */
    public void prepararBaseDatos(boolean datosEjemplo) {
        db.migrar();
        if (datosEjemplo && titulaciones.listar().isEmpty()) db.ejecutarScript("/db/datos-ejemplo.sql");
    }

    @Override
    public void close() {
        db.close();
    }
}
