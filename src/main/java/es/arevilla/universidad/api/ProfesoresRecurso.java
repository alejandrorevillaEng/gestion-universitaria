package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Profesor;
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

@Path("/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfesoresRecurso extends RecursoBase {

    @GET
    public List<Profesor> listar() {
        return servicios().profesores().listar();
    }

    @GET
    @Path("/{id}")
    public Profesor buscar(@PathParam("id") int id) {
        return servicios().profesores().buscar(id);
    }

    @POST
    public Response crear(Profesor datos) {
        Profesor p = servicios().profesores().crear(cuerpo(datos));
        return creado(p.getId(), p);
    }

    @PUT
    @Path("/{id}")
    public Profesor actualizar(@PathParam("id") int id, Profesor datos) {
        return servicios().profesores().actualizar(id, cuerpo(datos));
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().profesores().eliminar(id);
        return Response.noContent().build();
    }
}
