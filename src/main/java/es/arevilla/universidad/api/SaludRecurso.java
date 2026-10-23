package es.arevilla.universidad.api;

import es.arevilla.universidad.datos.Jdbc;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comprobación de salud para Docker y balanceadores. Es pública y no cuenta nada
 * del sistema: solo si la aplicación responde y llega a la base de datos.
 */
@Path("/salud")
@Produces(MediaType.APPLICATION_JSON)
public class SaludRecurso extends RecursoBase {

    private static final Logger LOG = Logger.getLogger(SaludRecurso.class.getName());

    @GET
    public Response salud() {
        try {
            servicios().db().transaccion(c -> Jdbc.entero(c, "SELECT 1"));
            return Response.ok(Map.of("estado", "ok")).build();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "La base de datos no responde", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("estado", "sin base de datos")).build();
        }
    }
}
