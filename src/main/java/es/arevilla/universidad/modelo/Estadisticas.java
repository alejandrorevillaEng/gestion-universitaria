package es.arevilla.universidad.modelo;

import java.util.List;

/** Datos del panel de estadísticas del administrador. */
public record Estadisticas(int totalAlumnos, int totalAsignaturas, int totalMatriculas,
                           List<Fila> asignaturasPorTitulacion, List<Asignatura> ocupacion) {

    public record Fila(String nombre, int valor) {
        public String getNombre() { return nombre; }
        public int getValor() { return valor; }
    }

    public int getTotalAlumnos() { return totalAlumnos; }
    public int getTotalAsignaturas() { return totalAsignaturas; }
    public int getTotalMatriculas() { return totalMatriculas; }
    public List<Fila> getAsignaturasPorTitulacion() { return asignaturasPorTitulacion; }
    public List<Asignatura> getOcupacion() { return ocupacion; }
}
