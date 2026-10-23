package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Aula;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AulasRecurso extends RecursoBase {

    @GET
    public List<Aula> listar() {
        return servicios().aulas().listar();
    }

    @GET
    @Path("/{id}")
    public Aula buscar(@PathParam("id") int id) {
        return servicios().aulas().buscar(id);
    }

    @POST
    public Response crear(Aula datos) {
        Aula a = servicios().aulas().crear(cuerpo(datos));
        return creado(a.getId(), a);
    }

    @PUT
    @Path("/{id}")
    public Aula actualizar(@PathParam("id") int id, Aula datos) {
        return servicios().aulas().actualizar(id, cuerpo(datos));
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().aulas().eliminar(id);
        return Response.noContent().build();
    }
}
