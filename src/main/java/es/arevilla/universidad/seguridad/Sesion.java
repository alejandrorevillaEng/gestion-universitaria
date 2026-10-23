package es.arevilla.universidad.seguridad;

import es.arevilla.universidad.modelo.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/** Lo que guarda la sesión: el usuario autenticado y el token CSRF. */
public final class Sesion {

    public static final String USUARIO = "usuario";
    public static final String TOKEN_CSRF = "csrfToken";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Sesion() {
    }

    public static Usuario usuario(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (Usuario) s.getAttribute(USUARIO);
    }

    /**
     * Inicia la sesión del usuario. Cambia el id de sesión para evitar la
     * fijación de sesión y genera un token CSRF nuevo.
     */
    public static void iniciar(HttpServletRequest req, Usuario usuario) {
        req.getSession(true);
        req.changeSessionId();
        HttpSession s = req.getSession();
        Usuario sinHash = new Usuario(usuario.getId(), usuario.getNombre(), usuario.getRol());
        s.setAttribute(USUARIO, sinHash);
        s.setAttribute(TOKEN_CSRF, nuevoToken());
    }

    public static void cerrar(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
    }

    /** Devuelve el token CSRF de la sesión, creándolo si aún no existe. */
    public static String tokenCsrf(HttpServletRequest req) {
        HttpSession s = req.getSession(true);
        String token = (String) s.getAttribute(TOKEN_CSRF);
        if (token == null) {
            token = nuevoToken();
            s.setAttribute(TOKEN_CSRF, token);
        }
        return token;
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
