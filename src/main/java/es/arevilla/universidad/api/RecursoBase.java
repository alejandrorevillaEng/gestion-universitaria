package es.arevilla.universidad.api;

import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Sesion;
import es.arevilla.universidad.servicio.ErrorNegocio;
import es.arevilla.universidad.servicio.Servicios;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/** Acceso a los servicios compartidos con la web y al usuario de la sesión. */
abstract class RecursoBase {

    @Context
    private ServletContext contexto;
    @Context
    protected HttpServletRequest peticion;
    @Context
    protected UriInfo uri;

    protected Servicios servicios() {
        return (Servicios) contexto.getAttribute(Servicios.ATRIBUTO);
    }

    protected Usuario usuario() {
        Usuario u = Sesion.usuario(peticion);
        if (u == null) throw ErrorNegocio.prohibido("Sin sesión");
        return u;
    }

    protected static <T> T cuerpo(T datos) {
        if (datos == null) throw ErrorNegocio.validacion("Falta el cuerpo JSON de la petición");
        return datos;
    }

    protected Response creado(int id, Object entidad) {
        URI ubicacion = uri.getAbsolutePathBuilder().path(Integer.toString(id)).build();
        return Response.created(ubicacion).entity(entidad).build();
    }
}
