package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Rol;
import es.arevilla.universidad.modelo.Usuario;
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

/** CRUD de usuarios. Solo administradores: lo comprueba FiltroAutenticacion antes de llegar aquí. */
@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuariosRecurso extends RecursoBase {

    public static class DatosUsuario {
        public String nombre;
        public String contrasena;
        public Rol rol;
    }

    @GET
    public List<Usuario> listar() {
        return servicios().usuarios().listar();
    }

    @GET
    @Path("/{id}")
    public Usuario buscar(@PathParam("id") int id) {
        return servicios().usuarios().buscar(id);
    }

    @POST
    public Response crear(DatosUsuario datos) {
        DatosUsuario d = cuerpo(datos);
        Usuario u = servicios().usuarios().crear(d.nombre, d.contrasena, d.rol);
        return creado(u.getId(), u);
    }

    @PUT
    @Path("/{id}")
    public Usuario actualizar(@PathParam("id") int id, DatosUsuario datos) {
        DatosUsuario d = cuerpo(datos);
        return servicios().usuarios().actualizar(id, d.nombre, d.contrasena, d.rol, usuario().getId());
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        servicios().usuarios().eliminar(id, usuario().getId());
        return Response.noContent().build();
    }
}
