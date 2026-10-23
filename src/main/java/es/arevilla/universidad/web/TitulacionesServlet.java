package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Titulacion;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/titulaciones")
public class TitulacionesServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> formulario(req, resp, new Titulacion(), null);
            case "editar" -> formulario(req, resp, servicios().titulaciones().buscar(id(req)), null);
            case "ver" -> {
                int id = id(req);
                req.setAttribute("titulacion", servicios().titulaciones().buscar(id));
                req.setAttribute("asignaturas", servicios().asignaturas().listarPorTitulacion(id));
                vista(req, resp, "titulaciones/detalle");
            }
            default -> {
                req.setAttribute("titulaciones", servicios().titulaciones().listar());
                vista(req, resp, "titulaciones/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("eliminar".equals(accion(req))) {
            try {
                servicios().titulaciones().eliminar(id(req));
                aviso(req, "Titulación borrada");
            } catch (ErrorNegocio e) {
                error(req, e.getMessage());
            }
            redirigir(req, resp, "/titulaciones");
            return;
        }
        int id = idOpcional(req);
        Titulacion datos = new Titulacion(id, req.getParameter("nombre"), req.getParameter("descripcion"));
        try {
            if (id == 0) servicios().titulaciones().crear(datos);
            else servicios().titulaciones().actualizar(id, datos);
            aviso(req, "Titulación «" + datos.getNombre().strip() + "» guardada");
            redirigir(req, resp, "/titulaciones");
        } catch (ErrorNegocio e) {
            formulario(req, resp, datos, e.getMessage());
        }
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Titulacion t, String error)
            throws ServletException, IOException {
        req.setAttribute("titulacion", t);
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "titulaciones/formulario");
    }
}
