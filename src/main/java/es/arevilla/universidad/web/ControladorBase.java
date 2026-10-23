package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Sesion;
import es.arevilla.universidad.servicio.ErrorNegocio;
import es.arevilla.universidad.servicio.Servicios;
import es.arevilla.universidad.servicio.Validar;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Base de los controladores MVC. Los servlets leen parámetros, llaman a los
 * servicios y eligen la vista; no tienen HTML ni SQL.
 *
 * Patrón POST-redirect-GET: tras guardar o borrar se redirige al listado con
 * un aviso en la sesión ("flash"), que la cabecera muestra una sola vez.
 */
public abstract class ControladorBase extends HttpServlet {

    protected Servicios servicios() {
        return (Servicios) getServletContext().getAttribute(Servicios.ATRIBUTO);
    }

    protected Usuario usuario(HttpServletRequest req) {
        return Sesion.usuario(req);
    }

    protected void vista(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/" + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    protected void aviso(HttpServletRequest req, String mensaje) {
        flash(req, "aviso", mensaje);
    }

    protected void error(HttpServletRequest req, String mensaje) {
        flash(req, "error", mensaje);
    }

    private static void flash(HttpServletRequest req, String tipo, String mensaje) {
        HttpSession s = req.getSession();
        s.setAttribute("flash", new Flash(tipo, mensaje));
    }

    protected static String accion(HttpServletRequest req) {
        String accion = req.getParameter("accion");
        return accion == null || accion.isBlank() ? "listar" : accion;
    }

    protected static int id(HttpServletRequest req) {
        return Validar.entero(req.getParameter("id"), "id");
    }

    /** Id del formulario: 0 si es un alta, el id si es una edición. */
    protected static int idOpcional(HttpServletRequest req) {
        String id = req.getParameter("id");
        return id == null || id.isBlank() ? 0 : Validar.entero(id, "id");
    }

    /** Texto de búsqueda (?q=) y página (?pagina=) de los listados. */
    protected static es.arevilla.universidad.servicio.Busqueda busqueda(HttpServletRequest req) {
        String pagina = req.getParameter("pagina");
        return es.arevilla.universidad.servicio.Busqueda.de(req.getParameter("q"),
                pagina == null || pagina.isBlank() ? null : Validar.entero(pagina, "página"), null);
    }

    protected static int entero(HttpServletRequest req, String campo) {
        return Validar.entero(req.getParameter(campo), campo);
    }

    /**
     * Un error de negocio en un GET (id inexistente, parámetro mal formado)
     * acaba en la página de error con su código, nunca en un 500 con traza.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (ErrorNegocio e) {
            if (!resp.isCommitted()) resp.sendError(e.getEstadoHttp(), e.getMessage());
        }
    }

    public record Flash(String tipo, String mensaje) implements java.io.Serializable {
        public String getTipo() { return tipo; }
        public String getMensaje() { return mensaje; }
    }
}
