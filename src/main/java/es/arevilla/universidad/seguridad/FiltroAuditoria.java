package es.arevilla.universidad.seguridad;

import es.arevilla.universidad.modelo.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Registro de auditoría: quién cambia qué y con qué resultado. Solo se
 * apuntan usuario, método, ruta, código HTTP e IP; nunca los parámetros, que
 * pueden llevar contraseñas.
 */
public class FiltroAuditoria extends HttpFilter {

    static final Logger AUDITORIA = Logger.getLogger("auditoria");
    private static final Set<String> METODOS_QUE_CAMBIAN = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (!METODOS_QUE_CAMBIAN.contains(req.getMethod())) {
            chain.doFilter(req, resp);
            return;
        }
        String antes = nombre(Sesion.usuario(req));
        try {
            chain.doFilter(req, resp);
        } finally {
            /* Tras el login el usuario aparece en la sesión: se apunta quién ha entrado. */
            String despues = nombre(Sesion.usuario(req));
            String usuario = antes.equals("-") ? despues : antes;
            AUDITORIA.info(() -> String.format("usuario=%s metodo=%s ruta=%s estado=%d ip=%s",
                    usuario, req.getMethod(), limpiar(FiltroAutenticacion.ruta(req)), resp.getStatus(),
                    req.getRemoteAddr()));
        }
    }

    private static String nombre(Usuario u) {
        return u == null ? "-" : limpiar(u.getNombre());
    }

    /** Evita que un valor con saltos de línea falsee entradas del registro (log injection). */
    static String limpiar(String texto) {
        return texto == null ? "-" : texto.replaceAll("[\\r\\n\\t]", "_");
    }
}
