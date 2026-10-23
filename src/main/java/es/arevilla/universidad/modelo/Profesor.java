package es.arevilla.universidad.modelo;

public class Profesor {
    private int id;
    private String nombre;
    private String especialidad;
    private int numAsignaturas;

    public Profesor() {
    }

    public Profesor(int id, String nombre, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    /** Solo lectura: número de asignaturas que imparte. */
    public int getNumAsignaturas() { return numAsignaturas; }
    public void setNumAsignaturas(int numAsignaturas) { this.numAsignaturas = numAsignaturas; }
}
