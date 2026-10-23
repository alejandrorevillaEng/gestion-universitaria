package es.arevilla.universidad.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Cabeceras de seguridad en todas las respuestas. La CSP solo permite
 * scripts y estilos propios: aunque se colara HTML en algún dato, el
 * navegador no ejecutaría scripts en línea.
 */
public class FiltroCabeceras extends HttpFilter {

    static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
            + "object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        resp.setHeader("Content-Security-Policy", CSP);
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        if (!FiltroAutenticacion.esEstatica(FiltroAutenticacion.ruta(req))) {
            resp.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(req, resp);
    }
}
