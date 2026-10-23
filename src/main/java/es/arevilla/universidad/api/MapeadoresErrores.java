package es.arevilla.universidad.api;

import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Convierte las excepciones en respuestas JSON {"error": "..."} con el código HTTP adecuado. */
public final class MapeadoresErrores {

    private static final Logger LOG = Logger.getLogger(MapeadoresErrores.class.getName());

    private MapeadoresErrores() {
    }

    static Response error(int estado, String mensaje) {
        return Response.status(estado).type(MediaType.APPLICATION_JSON).entity(Map.of("error", mensaje)).build();
    }

    @Provider
    public static class Negocio implements ExceptionMapper<ErrorNegocio> {
        @Override
        public Response toResponse(ErrorNegocio e) {
            return error(e.getEstadoHttp(), e.getMessage());
        }
    }

    /** 404 de rutas inexistentes, 405, 415... los genera Jersey: se respeta su código. */
    @Provider
    public static class Web implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException e) {
            Throwable causa = e.getCause();
            if (causa instanceof JsonbException || causa instanceof ProcessingException) {
                return error(400, "El cuerpo de la petición no es un JSON válido");
            }
            int estado = e.getResponse().getStatus();
            return error(estado, Response.Status.fromStatusCode(estado) == null
                    ? "Error " + estado : Response.Status.fromStatusCode(estado).getReasonPhrase());
        }
    }

    @Provider
    public static class Json implements ExceptionMapper<JsonbException> {
        @Override
        public Response toResponse(JsonbException e) {
            return error(400, "El cuerpo de la petición no es un JSON válido");
        }
    }

    /** Jersey envuelve el error de JSON-B al leer el cuerpo en una ProcessingException. */
    @Provider
    public static class Lectura implements ExceptionMapper<ProcessingException> {
        @Override
        public Response toResponse(ProcessingException e) {
            if (e.getCause() instanceof JsonbException) {
                return error(400, "El cuerpo de la petición no es un JSON válido");
            }
            LOG.log(Level.SEVERE, "Error procesando la petición", e);
            return error(500, "Error interno");
        }
    }

    /** Cualquier otra cosa: se registra y se responde un 500 genérico, sin detalles internos. */
    @Provider
    public static class Inesperado implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException e) {
            LOG.log(Level.SEVERE, "Error no controlado en la API", e);
            return error(500, "Error interno");
        }
    }
}
