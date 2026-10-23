package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Rol;
import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Gestión de usuarios. FiltroAutenticacion ya garantiza que solo entra un administrador. */
@WebServlet("/usuarios")
public class UsuariosServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> formulario(req, resp, new Usuario(), null);
            case "editar" -> formulario(req, resp, servicios().usuarios().buscar(id(req)), null);
            default -> {
                req.setAttribute("usuarios", servicios().usuarios().listar());
                vista(req, resp, "usuarios/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int yo = usuario(req).getId();
        if ("eliminar".equals(accion(req))) {
            try {
                servicios().usuarios().eliminar(id(req), yo);
                aviso(req, "Usuario borrado");
            } catch (ErrorNegocio e) {
                error(req, e.getMessage());
            }
            redirigir(req, resp, "/usuarios");
            return;
        }
        int id = idOpcional(req);
        Rol rol = "ADMIN".equals(req.getParameter("rol")) ? Rol.ADMIN : Rol.USUARIO;
        String nombre = req.getParameter("nombre");
        String contrasena = req.getParameter("contrasena");
        try {
            if (id == 0) servicios().usuarios().crear(nombre, contrasena, rol);
            else servicios().usuarios().actualizar(id, nombre, contrasena, rol, yo);
            aviso(req, "Usuario guardado");
            redirigir(req, resp, "/usuarios");
        } catch (ErrorNegocio e) {
            formulario(req, resp, new Usuario(id, nombre, rol), e.getMessage());
        }
    }

    /** El formulario nunca recibe la contraseña ni su hash: al editar, se deja en blanco para no cambiarla. */
    private void formulario(HttpServletRequest req, HttpServletResponse resp, Usuario u, String error)
            throws ServletException, IOException {
        req.setAttribute("usuarioEditado", new Usuario(u.getId(), u.getNombre(), u.getRol()));
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "usuarios/formulario");
    }
}
