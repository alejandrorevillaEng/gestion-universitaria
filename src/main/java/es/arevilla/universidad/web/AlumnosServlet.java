package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/alumnos")
public class AlumnosServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> formulario(req, resp, new Alumno(), null);
            case "editar" -> formulario(req, resp, servicios().alumnos().buscar(id(req)), null);
            case "ver" -> {
                int id = id(req);
                req.setAttribute("alumno", servicios().alumnos().buscar(id));
                req.setAttribute("asignaturas", servicios().matriculas().asignaturasDe(id));
                vista(req, resp, "alumnos/detalle");
            }
            default -> {
                var pagina = servicios().alumnos().buscar(busqueda(req));
                req.setAttribute("pagina", pagina);
                req.setAttribute("alumnos", pagina.elementos());
                vista(req, resp, "alumnos/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("eliminar".equals(accion(req))) {
            try {
                servicios().alumnos().eliminar(id(req));
                aviso(req, "Alumno borrado junto con sus matrículas");
            } catch (ErrorNegocio e) {
                error(req, e.getMessage());
            }
            redirigir(req, resp, "/alumnos");
            return;
        }
        int id = idOpcional(req);
        Alumno datos = new Alumno(id, req.getParameter("nombre"), req.getParameter("apellidos"),
                req.getParameter("email"));
        try {
            if (id == 0) servicios().alumnos().crear(datos);
            else servicios().alumnos().actualizar(id, datos);
            aviso(req, "Alumno guardado");
            redirigir(req, resp, "/alumnos");
        } catch (ErrorNegocio e) {
            formulario(req, resp, datos, e.getMessage());
        }
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Alumno a, String error)
            throws ServletException, IOException {
        req.setAttribute("alumno", a);
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "alumnos/formulario");
    }
}
