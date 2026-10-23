package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.servicio.Busqueda;
import es.arevilla.universidad.modelo.Asignatura;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/alumnos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlumnosRecurso extends RecursoBase {

    /** Sin parámetros devuelve todo; con q, pagina o tam filtra y pagina (total en X-Total-Count). */
    @GET
    public Response listar(@QueryParam("q") String q, @QueryParam("pagina") Integer pagina,
                           @QueryParam("tam") Integer tamano) {
        if (!pideBusqueda(q, pagina, tamano)) return Response.ok(servicios().alumnos().listar()).build();
        return pagina(servicios().alumnos().buscar(Busqueda.de(q, pagina, tamano)));
    }

    @GET
    @Path("/{id}")
    public Alumno buscar(@PathParam("id") int id) {
        return servicios().alumnos().buscar(id);
    }

    @GET
    @Path("/{id}/asignaturas")
    public List<Asignatura> asignaturas(@PathParam("id") int id) {
        servicios().alumnos().buscar(id);
        return servicios().matriculas().asignaturasDe(id);
    }

    @POST
    public Response crear(Alumno datos) {
        Alumno a = servicios().alumnos().crear(cuerpo(datos));
        return creado(a.getId(), a);
    }

    @PUT
    @Path("/{id}")
    public Alumno actualizar(@PathParam("id") int id, Alumno datos) {
        return servicios().alumnos().actualizar(id, cuerpo(datos));
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().alumnos().eliminar(id);
        return Response.noContent().build();
    }
}
