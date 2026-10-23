package es.arevilla.universidad.servicio;

import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Profesor;
import es.arevilla.universidad.modelo.Titulacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasNegocioTest {

    private Servicios s;
    private Titulacion grado;
    private Profesor profesor;

    @BeforeEach
    void preparar() {
        s = BaseDatosPrueba.nueva();
        grado = s.titulaciones().crear(new Titulacion(0, "Informática", "Grado"));
        profesor = s.profesores().crear(new Profesor(0, "Ana Ruiz", "Redes"));
    }

    @AfterEach
    void cerrar() {
        s.close();
    }

    private Asignatura asignatura(String nombre, int capacidad) {
        return s.asignaturas().crear(new Asignatura(0, nombre, 6, capacidad, grado.getId(), null));
    }

    private Alumno alumno(int n) {
        return s.alumnos().crear(new Alumno(0, "Alumno" + n, "Prueba", "alumno" + n + "@ejemplo.es"));
    }

    private static int estado(Runnable accion) {
        return assertThrows(ErrorNegocio.class, accion::run).getEstadoHttp();
    }

    @Test
    @DisplayName("No se puede matricular por encima de la capacidad máxima")
    void capacidadMaxima() {
        Asignatura a = asignatura("Redes", 2);
        s.matriculas().matricular(a.getId(), alumno(1).getId());
        s.matriculas().matricular(a.getId(), alumno(2).getId());
        int tercero = alumno(3).getId();

        assertEquals(409, estado(() -> s.matriculas().matricular(a.getId(), tercero)));
        assertEquals(2, s.asignaturas().buscar(a.getId()).getMatriculados());
    }

    @Test
    @DisplayName("Dos matrículas a la vez no se cuelan en la última plaza")
    void matriculasConcurrentes() throws Exception {
        Asignatura a = asignatura("Concurrencia", 1);
        List<Integer> alumnos = new ArrayList<>();
        for (int i = 0; i < 8; i++) alumnos.add(alumno(i).getId());

        ExecutorService hilos = Executors.newFixedThreadPool(8);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int idAlumno : alumnos) {
            resultados.add(hilos.submit(() -> {
                salida.await();
                try {
                    s.matriculas().matricular(a.getId(), idAlumno);
                    return true;
                } catch (ErrorNegocio e) {
                    return false;
                }
            }));
        }
        salida.countDown();
        int correctas = 0;
        for (Future<Boolean> r : resultados) if (r.get()) correctas++;
        hilos.shutdown();

        assertEquals(1, correctas);
        assertEquals(1, s.asignaturas().buscar(a.getId()).getMatriculados());
    }

    @Test
    @DisplayName("No se borra una asignatura con alumnos matriculados")
    void borrarAsignaturaConAlumnos() {
        Asignatura a = asignatura("Cálculo", 10);
        Alumno al = alumno(1);
        s.matriculas().matricular(a.getId(), al.getId());

        assertEquals(409, estado(() -> s.asignaturas().eliminar(a.getId())));

        s.matriculas().desmatricular(a.getId(), al.getId());
        s.asignaturas().eliminar(a.getId());
        assertEquals(404, estado(() -> s.asignaturas().buscar(a.getId())));
    }

    @Test
    @DisplayName("No se reduce la capacidad por debajo de los matriculados")
    void reducirCapacidad() {
        Asignatura a = asignatura("Física", 3);
        s.matriculas().matricular(a.getId(), alumno(1).getId());
        s.matriculas().matricular(a.getId(), alumno(2).getId());
        a.setCapacidad(1);
        assertEquals(409, estado(() -> s.asignaturas().actualizar(a.getId(), a)));
    }

    @Test
    @DisplayName("Ni titulaciones con asignaturas ni profesores con docencia se pueden borrar")
    void borradosConDependencias() {
        Asignatura a = asignatura("Algoritmia", 30);
        s.asignaturas().asignarProfesor(a.getId(), profesor.getId());

        assertEquals(409, estado(() -> s.titulaciones().eliminar(grado.getId())));
        assertEquals(409, estado(() -> s.profesores().eliminar(profesor.getId())));

        Asignatura sinProfesor = s.asignaturas().asignarProfesor(a.getId(), null);
        assertNull(sinProfesor.getIdProfesor());
        s.profesores().eliminar(profesor.getId());
    }

    @Test
    @DisplayName("Validación: campos obligatorios, rangos, referencias y duplicados")
    void validaciones() {
        assertEquals(400, estado(() -> s.titulaciones().crear(new Titulacion(0, "   ", null))));
        assertEquals(409, estado(() -> s.titulaciones().crear(new Titulacion(0, "informática", null))));
        assertEquals(400, estado(() -> s.asignaturas().crear(new Asignatura(0, "X", 6, 0, grado.getId(), null))));
        assertEquals(400, estado(() -> s.asignaturas().crear(new Asignatura(0, "X", 6, 10, 999, null))));
        assertEquals(400, estado(() -> s.asignaturas().crear(new Asignatura(0, "X", 6, 10, grado.getId(), 999))));
        assertEquals(400, estado(() -> s.alumnos().crear(new Alumno(0, "A", "B", "no-es-un-email"))));
        alumno(1);
        assertEquals(409, estado(() -> s.alumnos().crear(new Alumno(0, "Otro", "B", "ALUMNO1@ejemplo.es"))));
        assertEquals(404, estado(() -> s.asignaturas().asignarProfesor(1234, null)));
    }

    @Test
    @DisplayName("Al borrar un alumno se borran sus matrículas")
    void borrarAlumno() {
        Asignatura a = asignatura("Bases de datos", 5);
        Alumno al = alumno(1);
        s.matriculas().matricular(a.getId(), al.getId());
        s.alumnos().eliminar(al.getId());
        assertEquals(0, s.asignaturas().buscar(a.getId()).getMatriculados());
    }

    @Test
    @DisplayName("El CSV escapa separadores, comillas y fórmulas de Excel")
    void exportarCsv() {
        Asignatura a = asignatura("Seguridad", 5);
        int id = s.alumnos().crear(new Alumno(0, "=HYPERLINK(\"http://x\")", "López; García", "csv@ejemplo.es")).getId();
        s.matriculas().matricular(a.getId(), id);

        String csv = s.matriculas().exportarCsv(a.getId());

        assertTrue(csv.startsWith("asignatura;Seguridad\r\napellidos;nombre;email\r\n"));
        assertTrue(csv.contains("\"López; García\";\"'=HYPERLINK(\"\"http://x\"\")\";csv@ejemplo.es"), csv);
    }

    @Test
    @DisplayName("Las estadísticas cuentan alumnos, asignaturas por titulación y ocupación")
    void estadisticas() {
        Asignatura a = asignatura("Llena", 1);
        asignatura("Vacía", 10);
        s.matriculas().matricular(a.getId(), alumno(1).getId());

        var e = s.estadisticas().calcular();

        assertEquals(1, e.totalAlumnos());
        assertEquals(2, e.totalAsignaturas());
        assertEquals(1, e.totalMatriculas());
        assertEquals(2, e.asignaturasPorTitulacion().get(0).valor());
        assertEquals("Llena", e.ocupacion().get(0).getNombre());
        assertEquals(100, e.ocupacion().get(0).getOcupacion());
    }
}
