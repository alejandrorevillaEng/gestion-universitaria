package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Sesion;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/** Login, registro, logout y página de inicio. */
public final class AccesoServlets {

    private AccesoServlets() {
    }

    @WebServlet("/login")
    public static class Login extends ControladorBase {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (usuario(req) != null) {
                redirigir(req, resp, "/asignaturas");
                return;
            }
            vista(req, resp, "login");
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String nombre = req.getParameter("nombre");
            try {
                Optional<Usuario> u = servicios().usuarios().autenticar(nombre, req.getParameter("contrasena"));
                if (u.isPresent()) {
                    Sesion.iniciar(req, u.get());
                    redirigir(req, resp, "/asignaturas");
                    return;
                }
                req.setAttribute("errorFormulario", "Usuario o contraseña incorrectos");
            } catch (ErrorNegocio e) {
                req.setAttribute("errorFormulario", e.getMessage());
            }
            req.setAttribute("nombre", nombre);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            vista(req, resp, "login");
        }
    }

    @WebServlet("/registro")
    public static class Registro extends ControladorBase {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            vista(req, resp, "registro");
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String nombre = req.getParameter("nombre");
            String contrasena = req.getParameter("contrasena");
            try {
                if (contrasena == null || !contrasena.equals(req.getParameter("repetir"))) {
                    throw ErrorNegocio.validacion("Las contraseñas no coinciden");
                }
                servicios().usuarios().registrar(nombre, contrasena);
                aviso(req, "Cuenta creada. Ya puedes entrar.");
                redirigir(req, resp, "/login");
            } catch (ErrorNegocio e) {
                req.setAttribute("errorFormulario", e.getMessage());
                req.setAttribute("nombre", nombre);
                resp.setStatus(e.getEstadoHttp());
                vista(req, resp, "registro");
            }
        }
    }

    /** Solo por POST (con token CSRF): un enlace en otra web no puede cerrar la sesión. */
    @WebServlet("/logout")
    public static class Logout extends ControladorBase {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            Sesion.cerrar(req);
            redirigir(req, resp, "/login");
        }
    }

    @WebServlet("")
    public static class Inicio extends ControladorBase {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            redirigir(req, resp, "/asignaturas");
        }
    }

    @WebServlet("/estadisticas")
    public static class Estadisticas extends ControladorBase {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.setAttribute("estadisticas", servicios().estadisticas().calcular());
            vista(req, resp, "estadisticas");
        }
    }

    @WebServlet("/panel-api")
    public static class PanelApi extends ControladorBase {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            vista(req, resp, "panel-api");
        }
    }
}
