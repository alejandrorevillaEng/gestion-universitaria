package es.arevilla.universidad.modelo;

/**
 * Una asignatura pertenece a una sola titulación y la imparte como mucho un
 * profesor (idProfesor null = sin asignar). La capacidad limita las matrículas.
 */
public class Asignatura {
    private int id;
    private String nombre;
    private int creditos = 6;
    private int capacidad;
    private int idTitulacion;
    private Integer idProfesor;

    /* Campos de solo lectura que rellenan las consultas con JOIN. */
    private String nombreTitulacion;
    private String nombreProfesor;
    private int matriculados;

    public Asignatura() {
    }

    public Asignatura(int id, String nombre, int creditos, int capacidad, int idTitulacion,
                      Integer idProfesor) {
        this.id = id;
        this.nombre = nombre;
        this.creditos = creditos;
        this.capacidad = capacidad;
        this.idTitulacion = idTitulacion;
        this.idProfesor = idProfesor;
    }

    public int getPlazasLibres() {
        return Math.max(0, capacidad - matriculados);
    }

    /** Porcentaje de ocupación entre 0 y 100. */
    public int getOcupacion() {
        return capacidad == 0 ? 0 : (int) Math.round(100.0 * matriculados / capacidad);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public int getIdTitulacion() { return idTitulacion; }
    public void setIdTitulacion(int idTitulacion) { this.idTitulacion = idTitulacion; }
    public Integer getIdProfesor() { return idProfesor; }
    public void setIdProfesor(Integer idProfesor) { this.idProfesor = idProfesor; }
    public String getNombreTitulacion() { return nombreTitulacion; }
    public void setNombreTitulacion(String nombreTitulacion) { this.nombreTitulacion = nombreTitulacion; }
    public String getNombreProfesor() { return nombreProfesor; }
    public void setNombreProfesor(String nombreProfesor) { this.nombreProfesor = nombreProfesor; }
    public int getMatriculados() { return matriculados; }
    public void setMatriculados(int matriculados) { this.matriculados = matriculados; }
}
