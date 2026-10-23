package es.arevilla.universidad.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Protección CSRF con token sincronizado: toda petición que cambia datos
 * (POST, PUT, PATCH, DELETE) tiene que traer el token de la sesión, en el
 * campo oculto _csrf de los formularios o en la cabecera X-CSRF-Token de la API.
 * El token se deja en la petición para que las vistas lo pinten.
 */
public class FiltroCsrf extends HttpFilter {

    public static final String CAMPO = "_csrf";
    public static final String CABECERA = "X-CSRF-Token";
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");
    /** El login de la API crea la sesión y devuelve el token; aún no hay token que comprobar. */
    private static final Set<String> EXENTAS = Set.of("/api/sesion");

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String ruta = FiltroAutenticacion.ruta(req);
        if (!FiltroAutenticacion.esEstatica(ruta)) req.setAttribute(Sesion.TOKEN_CSRF, Sesion.tokenCsrf(req));

        boolean cambiaDatos = !METODOS_SEGUROS.contains(req.getMethod());
        boolean exenta = EXENTAS.contains(ruta) && "POST".equals(req.getMethod());
        if (cambiaDatos && !exenta && !tokenValido(req)) {
            if (ruta.startsWith("/api/")) {
                Respuestas.errorJson(resp, HttpServletResponse.SC_FORBIDDEN, "Falta el token CSRF o no es válido");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "La sesión ha caducado o el formulario no es válido");
            }
            return;
        }
        chain.doFilter(req, resp);
    }

    private static boolean tokenValido(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        String esperado = s == null ? null : (String) s.getAttribute(Sesion.TOKEN_CSRF);
        String recibido = req.getHeader(CABECERA);
        if (recibido == null) recibido = req.getParameter(CAMPO);
        return esperado != null && recibido != null
                && MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                                         recibido.getBytes(StandardCharsets.UTF_8));
    }
}
