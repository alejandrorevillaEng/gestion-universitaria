package es.arevilla.universidad.seguridad;

import es.arevilla.universidad.modelo.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Control de acceso de toda la aplicación (web y API).
 *
 * La ruta se calcula con getServletPath() + getPathInfo(), que el contenedor
 * ya ha normalizado y sin parámetros de ruta. La versión original miraba
 * getRequestURI() con endsWith(".css"), y una URL como /titulaciones;x.css
 * se saltaba el login.
 */
public class FiltroAutenticacion extends HttpFilter {

    private static final Set<String> PUBLICAS = Set.of("/login", "/registro", "/api/sesion", "/favicon.ico");
    private static final List<String> PREFIJOS_ESTATICOS = List.of("/css/", "/js/", "/img/", "/fuentes/");
    private static final List<String> SOLO_ADMIN = List.of("/usuarios", "/estadisticas", "/api/usuarios",
            "/api/estadisticas");

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String ruta = ruta(req);
        if (esPublica(ruta)) {
            chain.doFilter(req, resp);
            return;
        }
        Usuario usuario = Sesion.usuario(req);
        boolean api = ruta.startsWith("/api/");
        if (usuario == null) {
            if (api) {
                Respuestas.errorJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "Inicia sesión primero (POST /api/sesion)");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }
        if (requiereAdmin(ruta) && !usuario.esAdmin()) {
            if (api) Respuestas.errorJson(resp, HttpServletResponse.SC_FORBIDDEN, "Solo para administradores");
            else resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, resp);
    }

    static String ruta(HttpServletRequest req) {
        String servletPath = req.getServletPath() == null ? "" : req.getServletPath();
        String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
        return servletPath + pathInfo;
    }

    static boolean esPublica(String ruta) {
        return PUBLICAS.contains(ruta) || esEstatica(ruta);
    }

    static boolean esEstatica(String ruta) {
        return PREFIJOS_ESTATICOS.stream().anyMatch(ruta::startsWith) || ruta.equals("/favicon.ico");
    }

    static boolean requiereAdmin(String ruta) {
        return SOLO_ADMIN.stream().anyMatch(p -> ruta.equals(p) || ruta.startsWith(p + "/"));
    }
}
