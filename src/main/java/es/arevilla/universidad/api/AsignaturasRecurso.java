package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.modelo.Asignatura;
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

/**
 * Asignaturas, asignación de profesor y matrículas. Las dos relaciones son
 * subrecursos propios, así no hace falta reenviar la asignatura entera:
 *   PUT    /asignaturas/{id}/profesor/{idProfesor}
 *   DELETE /asignaturas/{id}/profesor
 *   PUT    /asignaturas/{id}/alumnos/{idAlumno}   (matricular)
 *   DELETE /asignaturas/{id}/alumnos/{idAlumno}   (desmatricular)
 */
@Path("/asignaturas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AsignaturasRecurso extends RecursoBase {

    @GET
    public List<Asignatura> listar() {
        return servicios().asignaturas().listar();
    }

    @GET
    @Path("/{id}")
    public Asignatura buscar(@PathParam("id") int id) {
        return servicios().asignaturas().buscar(id);
    }

    @POST
    public Response crear(Asignatura datos) {
        Asignatura a = servicios().asignaturas().crear(cuerpo(datos));
        return creado(a.getId(), a);
    }

    @PUT
    @Path("/{id}")
    public Asignatura actualizar(@PathParam("id") int id, Asignatura datos) {
        return servicios().asignaturas().actualizar(id, cuerpo(datos));
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().asignaturas().eliminar(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/profesor/{idProfesor}")
    public Asignatura asignarProfesor(@PathParam("id") int id, @PathParam("idProfesor") int idProfesor) {
        return servicios().asignaturas().asignarProfesor(id, idProfesor);
    }

    @DELETE
    @Path("/{id}/profesor")
    public Asignatura quitarProfesor(@PathParam("id") int id) {
        return servicios().asignaturas().asignarProfesor(id, null);
    }

    @GET
    @Path("/{id}/alumnos")
    public List<Alumno> alumnos(@PathParam("id") int id) {
        return servicios().matriculas().alumnosDe(id);
    }

    @GET
    @Path("/{id}/alumnos.csv")
    @Produces("text/csv")
    public Response alumnosCsv(@PathParam("id") int id) {
        return Response.ok(servicios().matriculas().exportarCsv(id))
                .type("text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"matriculados-" + id + ".csv\"")
                .build();
    }

    @PUT
    @Path("/{id}/alumnos/{idAlumno}")
    public Response matricular(@PathParam("id") int id, @PathParam("idAlumno") int idAlumno) {
        servicios().matriculas().matricular(id, idAlumno);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/alumnos/{idAlumno}")
    public Response desmatricular(@PathParam("id") int id, @PathParam("idAlumno") int idAlumno) {
        servicios().matriculas().desmatricular(id, idAlumno);
        return Response.noContent().build();
    }
}
