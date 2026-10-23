package es.arevilla.universidad.web;

import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.servicio.ErrorNegocio;
import es.arevilla.universidad.servicio.Validar;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@WebServlet("/asignaturas")
public class AsignaturasServlet extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "nuevo" -> {
                Asignatura nueva = new Asignatura();
                String tit = req.getParameter("titulacion");
                if (tit != null && !tit.isBlank()) nueva.setIdTitulacion(Validar.entero(tit, "titulacion"));
                formulario(req, resp, nueva, null);
            }
            case "editar" -> formulario(req, resp, servicios().asignaturas().buscar(id(req)), null);
            case "ver" -> detalle(req, resp, id(req));
            case "exportar" -> exportarCsv(req, resp, id(req));
            default -> {
                req.setAttribute("asignaturas", servicios().asignaturas().listar());
                vista(req, resp, "asignaturas/lista");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (accion(req)) {
            case "eliminar" -> {
                try {
                    servicios().asignaturas().eliminar(id(req));
                    aviso(req, "Asignatura borrada");
                } catch (ErrorNegocio e) {
                    error(req, e.getMessage());
                }
                redirigir(req, resp, "/asignaturas");
            }
            case "matricular", "desmatricular" -> {
                int id = id(req);
                try {
                    int alumno = entero(req, "alumno");
                    if ("matricular".equals(accion(req))) {
                        servicios().matriculas().matricular(id, alumno);
                        aviso(req, "Alumno matriculado");
                    } else {
                        servicios().matriculas().desmatricular(id, alumno);
                        aviso(req, "Alumno desmatriculado");
                    }
                } catch (ErrorNegocio e) {
                    error(req, e.getMessage());
                }
                redirigir(req, resp, "/asignaturas?accion=ver&id=" + id);
            }
            default -> guardar(req, resp);
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = idOpcional(req);
        Asignatura datos = new Asignatura(id, req.getParameter("nombre"), 0, 0, 0, null);
        try {
            datos.setCreditos(entero(req, "creditos"));
            datos.setCapacidad(entero(req, "capacidad"));
            datos.setIdTitulacion(entero(req, "idTitulacion"));
            datos.setIdProfesor(Validar.enteroOpcional(req.getParameter("idProfesor"), "profesor"));
            Asignatura guardada = id == 0
                    ? servicios().asignaturas().crear(datos)
                    : servicios().asignaturas().actualizar(id, datos);
            aviso(req, "Asignatura «" + guardada.getNombre() + "» guardada");
            redirigir(req, resp, "/asignaturas?accion=ver&id=" + guardada.getId());
        } catch (ErrorNegocio e) {
            formulario(req, resp, datos, e.getMessage());
        }
    }

    private void detalle(HttpServletRequest req, HttpServletResponse resp, int id)
            throws ServletException, IOException {
        req.setAttribute("asignatura", servicios().asignaturas().buscar(id));
        req.setAttribute("matriculados", servicios().matriculas().alumnosDe(id));
        req.setAttribute("candidatos", servicios().matriculas().alumnosNoMatriculadosEn(id));
        vista(req, resp, "asignaturas/detalle");
    }

    private void exportarCsv(HttpServletRequest req, HttpServletResponse resp, int id) throws IOException {
        String nombre = servicios().asignaturas().buscar(id).getNombre();
        String csv = servicios().matriculas().exportarCsv(id);
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"matriculados-" + nombreFichero(nombre) + ".csv\"");
        /* BOM para que Excel abra bien las tildes. */
        resp.getOutputStream().write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        resp.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
    }

    /** Nombre de fichero seguro para la cabecera: solo ASCII, sin comillas ni saltos de línea. */
    static String nombreFichero(String nombre) {
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String limpio = sinTildes.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return limpio.isEmpty() ? "asignatura" : limpio;
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Asignatura a, String error)
            throws ServletException, IOException {
        req.setAttribute("asignatura", a);
        req.setAttribute("titulaciones", servicios().titulaciones().listar());
        req.setAttribute("profesores", servicios().profesores().listar());
        req.setAttribute("errorFormulario", error);
        vista(req, resp, "asignaturas/formulario");
    }
}
