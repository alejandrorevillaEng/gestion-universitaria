package es.arevilla.universidad.servicio;

import es.arevilla.universidad.config.Configuracion;
import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Aula;
import es.arevilla.universidad.modelo.Titulacion;
import es.arevilla.universidad.seguridad.LimiteIntentos;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Las reglas que dependen del motor de base de datos, contra el MySQL 8.4 de producción:
 * migraciones de Flyway, LIKE con ESCAPE y el bloqueo FOR UPDATE de las matrículas.
 * Necesita Docker; se lanza con ./mvnw -Pmysql test (en CI va en su propio job).
 */
@Tag("mysql")
@Testcontainers
class MySqlTest {

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    private static Servicios s;
    private static Titulacion grado;

    @BeforeAll
    static void preparar() {
        Configuracion config = new Configuracion(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword(), null);
        s = Servicios.crear(BaseDatos.crear(config), new LimiteIntentos(3, Duration.ofMinutes(5), Clock.systemUTC()));
        s.prepararBaseDatos(true);
        grado = s.titulaciones().crear(new Titulacion(0, "Grado MySQL", "Pruebas"));
    }

    @AfterAll
    static void cerrar() {
        if (s != null) s.close();
    }

    @Test
    @DisplayName("Flyway crea el esquema en MySQL y los datos de ejemplo cargan")
    void migraciones() {
        List<String> aulas = s.aulas().listar().stream().map(Aula::getNombre).toList();
        assertTrue(aulas.containsAll(List.of("A-101", "A-204", "L-03")), aulas.toString());
        assertEquals(1, s.asignaturas().buscar(Busqueda.de("Programación I", null, null)).total());
    }

    @Test
    @DisplayName("En MySQL la búsqueda también trata %, _ y ! como texto")
    void comodines() {
        s.asignaturas().crear(new Asignatura(0, "Aprobado al 100%", 6, 10, grado.getId(), null));
        s.asignaturas().crear(new Asignatura(0, "Nota_final", 6, 10, grado.getId(), null));
        s.asignaturas().crear(new Asignatura(0, "¡Atención!", 6, 10, grado.getId(), null));

        assertEquals(1, s.asignaturas().buscar(Busqueda.de("100%", null, null)).total());
        assertEquals(1, s.asignaturas().buscar(Busqueda.de("a_f", null, null)).total());
        assertEquals(1, s.asignaturas().buscar(Busqueda.de("ción!", null, null)).total());
    }

    @Test
    @DisplayName("En MySQL las aulas respetan la capacidad y las claves ajenas")
    void aulas() {
        Asignatura a = s.asignaturas().crear(new Asignatura(0, "Sistemas MySQL", 6, 50, grado.getId(), null));
        Aula pequena = s.aulas().crear(new Aula(0, "M-1", null, 30));
        Aula grande = s.aulas().crear(new Aula(0, "M-2", null, 80));

        assertEquals(409, assertThrows(ErrorNegocio.class, () -> s.aulas().asignar(a.getId(), pequena.getId())).getEstadoHttp());
        s.aulas().asignar(a.getId(), grande.getId());
        s.asignaturas().eliminar(a.getId());
        assertEquals(0, s.aulas().buscar(grande.getId()).getNumAsignaturas(), "ON DELETE CASCADE en asignatura_aula");
    }

    @Test
    @DisplayName("En MySQL dos matrículas simultáneas tampoco se cuelan en la última plaza")
    void matriculasConcurrentes() throws Exception {
        Asignatura a = s.asignaturas().crear(new Asignatura(0, "Concurrencia MySQL", 6, 1, grado.getId(), null));
        List<Integer> alumnos = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            alumnos.add(s.alumnos().crear(new Alumno(0, "M" + i, "MySQL", "mysql" + i + "@ejemplo.es")).getId());
        }
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
}
