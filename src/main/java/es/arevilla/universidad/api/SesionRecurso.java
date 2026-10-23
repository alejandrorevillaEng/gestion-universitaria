package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Sesion;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Inicio de sesión para clientes de la API (curl, Postman). Devuelve el token
 * CSRF que hay que mandar en la cabecera X-CSRF-Token en las peticiones que
 * cambian datos. La cookie de sesión la gestiona el contenedor.
 */
@Path("/sesion")
@Produces(MediaType.APPLICATION_JSON)
public class SesionRecurso extends RecursoBase {

    public static class Credenciales {
        public String usuario;
        public String contrasena;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response entrar(Credenciales credenciales) {
        Credenciales c = cuerpo(credenciales);
        Usuario u = servicios().usuarios().autenticar(c.usuario, c.contrasena)
                .orElseThrow(() -> new ErrorNoAutenticado());
        Sesion.iniciar(peticion, u);
        return Response.ok(estado(u)).build();
    }

    @GET
    public Map<String, Object> actual() {
        return estado(usuario());
    }

    @DELETE
    public Response salir() {
        Sesion.cerrar(peticion);
        return Response.noContent().build();
    }

    private Map<String, Object> estado(Usuario u) {
        return Map.of("usuario", new Usuario(u.getId(), u.getNombre(), u.getRol()),
                "csrfToken", Sesion.tokenCsrf(peticion));
    }

    static class ErrorNoAutenticado extends ErrorNegocio {
        ErrorNoAutenticado() {
            super(401, "Usuario o contraseña incorrectos");
        }
    }
}
