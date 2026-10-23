package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.modelo.Asignatura;

import java.util.List;

public class ServicioMatriculas {

    private final BaseDatos db;

    public ServicioMatriculas(BaseDatos db) {
        this.db = db;
    }

    /**
     * Matricula a un alumno respetando la capacidad máxima. La fila de la
     * asignatura se bloquea (SELECT ... FOR UPDATE) mientras se cuentan las
     * plazas, así que dos matrículas simultáneas no pueden colarse en la última plaza.
     */
    public void matricular(int idAsignatura, int idAlumno) {
        db.transaccion(c -> {
            int capacidad = Jdbc.uno(c, "SELECT capacidad FROM asignatura WHERE id = ? FOR UPDATE",
                            rs -> rs.getInt(1), idAsignatura)
                    .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe la asignatura " + idAsignatura));
            if (Jdbc.entero(c, "SELECT COUNT(*) FROM alumno WHERE id = ?", idAlumno) == 0) {
                throw ErrorNegocio.noEncontrado("No existe el alumno " + idAlumno);
            }
            if (Jdbc.entero(c, "SELECT COUNT(*) FROM matricula WHERE id_alumno = ? AND id_asignatura = ?",
                    idAlumno, idAsignatura) > 0) {
                throw ErrorNegocio.conflicto("El alumno ya está matriculado en esta asignatura");
            }
            int matriculados = Jdbc.entero(c, "SELECT COUNT(*) FROM matricula WHERE id_asignatura = ?", idAsignatura);
            if (matriculados >= capacidad) {
                throw ErrorNegocio.conflicto("La asignatura está completa (" + capacidad + " plazas)");
            }
            Jdbc.actualizar(c, "INSERT INTO matricula (id_alumno, id_asignatura) VALUES (?, ?)",
                    idAlumno, idAsignatura);
            return null;
        });
    }

    public void desmatricular(int idAsignatura, int idAlumno) {
        db.transaccion(c -> {
            if (Jdbc.actualizar(c, "DELETE FROM matricula WHERE id_alumno = ? AND id_asignatura = ?",
                    idAlumno, idAsignatura) == 0) {
                throw ErrorNegocio.noEncontrado("El alumno no está matriculado en esta asignatura");
            }
            return null;
        });
    }

    public List<Alumno> alumnosDe(int idAsignatura) {
        return db.transaccion(c -> {
            ServicioAsignaturas.buscar(c, idAsignatura);
            return Jdbc.lista(c, ServicioAlumnos.SELECT + """
                     WHERE al.id IN (SELECT id_alumno FROM matricula WHERE id_asignatura = ?)
                     ORDER BY al.apellidos, al.nombre""", ServicioAlumnos::mapear, idAsignatura);
        });
    }

    /** Alumnos que aún no están en la asignatura (para el desplegable de matricular). */
    public List<Alumno> alumnosNoMatriculadosEn(int idAsignatura) {
        return db.transaccion(c -> Jdbc.lista(c, ServicioAlumnos.SELECT + """
                 WHERE al.id NOT IN (SELECT id_alumno FROM matricula WHERE id_asignatura = ?)
                 ORDER BY al.apellidos, al.nombre""", ServicioAlumnos::mapear, idAsignatura));
    }

    public List<Asignatura> asignaturasDe(int idAlumno) {
        return db.transaccion(c -> Jdbc.lista(c, ServicioAsignaturas.SELECT + """
                 WHERE a.id IN (SELECT id_asignatura FROM matricula WHERE id_alumno = ?)
                 ORDER BY a.nombre""", ServicioAsignaturas::mapear, idAlumno));
    }

    /** Listado de matriculados en CSV (requisito de la convocatoria de julio). */
    public String exportarCsv(int idAsignatura) {
        Asignatura asignatura = buscar(idAsignatura);
        StringBuilder csv = new StringBuilder();
        csv.append("asignatura;").append(Csv.campo(asignatura.getNombre())).append("\r\n");
        csv.append("apellidos;nombre;email\r\n");
        for (Alumno a : alumnosDe(idAsignatura)) {
            csv.append(Csv.campo(a.getApellidos())).append(';')
               .append(Csv.campo(a.getNombre())).append(';')
               .append(Csv.campo(a.getEmail())).append("\r\n");
        }
        return csv.toString();
    }

    private Asignatura buscar(int id) {
        return db.transaccion(c -> ServicioAsignaturas.buscar(c, id));
    }
}
