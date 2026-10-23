package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Titulacion;
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

@Path("/titulaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TitulacionesRecurso extends RecursoBase {

    @GET
    public List<Titulacion> listar() {
        return servicios().titulaciones().listar();
    }

    @GET
    @Path("/{id}")
    public Titulacion buscar(@PathParam("id") int id) {
        return servicios().titulaciones().buscar(id);
    }

    @GET
    @Path("/{id}/asignaturas")
    public List<Asignatura> asignaturas(@PathParam("id") int id) {
        servicios().titulaciones().buscar(id);
        return servicios().asignaturas().listarPorTitulacion(id);
    }

    @POST
    public Response crear(Titulacion datos) {
        Titulacion t = servicios().titulaciones().crear(cuerpo(datos));
        return creado(t.getId(), t);
    }

    @PUT
    @Path("/{id}")
    public Titulacion actualizar(@PathParam("id") int id, Titulacion datos) {
        return servicios().titulaciones().actualizar(id, cuerpo(datos));
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().titulaciones().eliminar(id);
        return Response.noContent().build();
    }
}
