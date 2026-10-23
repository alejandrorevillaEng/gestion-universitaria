package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Aula;
import es.arevilla.universidad.servicio.ErrorNegocio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/aulas")
public class AulasServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> formulario(req, resp, new Aula(), null);
            case "editar" -> formulario(req, resp, servicios().aulas().buscar(id(req)), null);
            default -> {
                req.setAttribute("aulas", servicios().aulas().listar());
                vista(req, resp, "aulas/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("eliminar".equals(accion(req))) {
            try {
                servicios().aulas().eliminar(id(req));
                aviso(req, "Aula borrada");
            } catch (ErrorNegocio e) {
                error(req, e.getMessage());
            }
            redirigir(req, resp, "/aulas");
            return;
        }
        int id = idOpcional(req);
        Aula datos = new Aula(id, req.getParameter("nombre"), req.getParameter("edificio"), 0);
        try {
            datos.setCapacidad(entero(req, "capacidad"));
            if (id == 0) servicios().aulas().crear(datos);
            else servicios().aulas().actualizar(id, datos);
            aviso(req, "Aula guardada");
            redirigir(req, resp, "/aulas");
        } catch (ErrorNegocio e) {
            formulario(req, resp, datos, e.getMessage());
        }
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Aula a, String error)
            throws ServletException, IOException {
        req.setAttribute("aula", a);
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "aulas/formulario");
    }
}
