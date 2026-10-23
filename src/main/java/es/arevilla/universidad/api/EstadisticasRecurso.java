package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Estadisticas;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/estadisticas")
@Produces(MediaType.APPLICATION_JSON)
public class EstadisticasRecurso extends RecursoBase {

    @GET
    public Estadisticas calcular() {
        return servicios().estadisticas().calcular();
    }
}
