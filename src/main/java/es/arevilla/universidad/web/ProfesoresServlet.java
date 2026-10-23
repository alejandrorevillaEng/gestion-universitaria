package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Profesor;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/profesores")
public class ProfesoresServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> formulario(req, resp, new Profesor(), null);
            case "editar" -> formulario(req, resp, servicios().profesores().buscar(id(req)), null);
            default -> {
                req.setAttribute("profesores", servicios().profesores().listar());
                vista(req, resp, "profesores/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("eliminar".equals(accion(req))) {
            try {
                servicios().profesores().eliminar(id(req));
                aviso(req, "Profesor borrado");
            } catch (ErrorNegocio e) {
                error(req, e.getMessage());
            }
            redirigir(req, resp, "/profesores");
            return;
        }
        int id = idOpcional(req);
        Profesor datos = new Profesor(id, req.getParameter("nombre"), req.getParameter("especialidad"));
        try {
            if (id == 0) servicios().profesores().crear(datos);
            else servicios().profesores().actualizar(id, datos);
            aviso(req, "Profesor «" + datos.getNombre().strip() + "» guardado");
            redirigir(req, resp, "/profesores");
        } catch (ErrorNegocio e) {
            formulario(req, resp, datos, e.getMessage());
        }
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Profesor p, String error)
            throws ServletException, IOException {
        req.setAttribute("profesor", p);
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "profesores/formulario");
    }
}
