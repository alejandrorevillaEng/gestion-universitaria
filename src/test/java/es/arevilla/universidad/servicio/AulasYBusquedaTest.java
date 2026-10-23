package es.arevilla.universidad.servicio;

import es.arevilla.universidad.modelo.Alumno;
import es.arevilla.universidad.modelo.Asignatura;
import es.arevilla.universidad.modelo.Aula;
import es.arevilla.universidad.modelo.Pagina;
import es.arevilla.universidad.modelo.Titulacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AulasYBusquedaTest {

    private Servicios s;
    private Titulacion grado;

    @BeforeEach
    void preparar() {
        s = BaseDatosPrueba.nueva();
        grado = s.titulaciones().crear(new Titulacion(0, "Informática", "Grado"));
    }

    @AfterEach
    void cerrar() {
        s.close();
    }

    private Asignatura asignatura(String nombre, int capacidad) {
        return s.asignaturas().crear(new Asignatura(0, nombre, 6, capacidad, grado.getId(), null));
    }

    private Aula aula(String nombre, int capacidad) {
        return s.aulas().crear(new Aula(0, nombre, "Edificio A", capacidad));
    }

    private static int estado(Runnable accion) {
        return assertThrows(ErrorNegocio.class, accion::run).getEstadoHttp();
    }

    @Test
    @DisplayName("Una asignatura no se asigna a un aula donde no caben sus alumnos")
    void aulaDemasiadoPequena() {
        Asignatura redes = asignatura("Redes", 40);
        Aula pequena = aula("L-01", 24);
        Aula grande = aula("A-101", 60);

        assertEquals(409, estado(() -> s.aulas().asignar(redes.getId(), pequena.getId())));
        s.aulas().asignar(redes.getId(), grande.getId());

        assertEquals(1, s.aulas().aulasDe(redes.getId()).size());
        assertEquals(409, estado(() -> s.aulas().asignar(redes.getId(), grande.getId())), "No se asigna dos veces");
        assertTrue(s.aulas().aulasDisponiblesPara(redes.getId()).isEmpty(),
                "El desplegable no ofrece aulas pequeñas ni las ya asignadas");
    }

    @Test
    @DisplayName("Las capacidades de aula y asignatura no se pueden dejar incoherentes")
    void capacidadesCoherentes() {
        Asignatura redes = asignatura("Redes", 40);
        Aula a101 = aula("A-101", 50);
        s.aulas().asignar(redes.getId(), a101.getId());

        Aula menor = new Aula(0, "A-101", "Edificio A", 30);
        assertEquals(409, estado(() -> s.aulas().actualizar(a101.getId(), menor)));

        Asignatura masGrande = new Asignatura(0, "Redes", 6, 55, grado.getId(), null);
        assertEquals(409, estado(() -> s.asignaturas().actualizar(redes.getId(), masGrande)));
    }

    @Test
    @DisplayName("Un aula con asignaturas no se borra; sin ellas, sí")
    void borrarAula() {
        Asignatura redes = asignatura("Redes", 20);
        Aula a101 = aula("A-101", 50);
        s.aulas().asignar(redes.getId(), a101.getId());

        assertEquals(409, estado(() -> s.aulas().eliminar(a101.getId())));
        s.aulas().quitar(redes.getId(), a101.getId());
        s.aulas().eliminar(a101.getId());
        assertEquals(404, estado(() -> s.aulas().buscar(a101.getId())));
    }

    @Test
    @DisplayName("Borrar una asignatura libera sus aulas")
    void borrarAsignaturaLiberaAulas() {
        Asignatura redes = asignatura("Redes", 20);
        Aula a101 = aula("A-101", 50);
        s.aulas().asignar(redes.getId(), a101.getId());

        s.asignaturas().eliminar(redes.getId());
        assertEquals(0, s.aulas().buscar(a101.getId()).getNumAsignaturas());
    }

    @Test
    @DisplayName("Nombres de aula únicos sin distinguir mayúsculas y datos validados")
    void validacionAulas() {
        aula("A-101", 50);
        assertEquals(409, estado(() -> aula("a-101", 30)));
        assertEquals(400, estado(() -> aula("", 30)));
        assertEquals(400, estado(() -> aula("B-1", 0)));
    }

    @Test
    @DisplayName("La búsqueda trata % y _ como texto, no como comodines")
    void busquedaSinComodines() {
        asignatura("Redes", 20);
        asignatura("Prácticas al 100% online", 20);
        asignatura("Diseño_web", 20);

        assertEquals(1, s.asignaturas().buscar(Busqueda.de("%", null, null)).total());
        assertEquals(1, s.asignaturas().buscar(Busqueda.de("_", null, null)).total());
        assertEquals(1, s.asignaturas().buscar(Busqueda.de("REDES", null, null)).total(), "No distingue mayúsculas");
        assertEquals(3, s.asignaturas().buscar(Busqueda.de("  ", null, null)).total(), "En blanco es sin filtro");
    }

    @Test
    @DisplayName("La paginación devuelve el trozo pedido y el total real")
    void paginacion() {
        for (int i = 0; i < 25; i++) {
            s.alumnos().crear(new Alumno(0, "Nombre" + i, String.format("Apellido%02d", i), "a" + i + "@ejemplo.es"));
        }
        Pagina<Alumno> tercera = s.alumnos().buscar(Busqueda.de(null, 3, 10));

        assertEquals(25, tercera.total());
        assertEquals(5, tercera.elementos().size());
        assertEquals(3, tercera.totalPaginas());
        assertTrue(tercera.hayAnterior());
        assertFalse(tercera.haySiguiente());
        assertEquals("Apellido20", tercera.elementos().get(0).getApellidos(), "Mismo orden en todas las páginas");
    }

    @Test
    @DisplayName("Los parámetros de búsqueda fuera de rango se rechazan")
    void parametrosDeBusqueda() {
        assertEquals(400, estado(() -> Busqueda.de(null, 0, null)));
        assertEquals(400, estado(() -> Busqueda.de(null, 1, Busqueda.TAMANO_MAXIMO + 1)));
        assertEquals(400, estado(() -> Busqueda.de("x".repeat(101), null, null)));
        assertEquals(Busqueda.TAMANO_POR_DEFECTO, Busqueda.de(null, null, null).tamano());
    }
}
