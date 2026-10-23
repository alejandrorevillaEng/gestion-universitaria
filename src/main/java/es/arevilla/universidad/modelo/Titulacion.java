package es.arevilla.universidad.modelo;

public class Titulacion {
    private int id;
    private String nombre;
    private String descripcion;
    private int numAsignaturas;

    public Titulacion() {
    }

    public Titulacion(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Solo lectura: lo rellena la consulta del listado. */
    public int getNumAsignaturas() { return numAsignaturas; }
    public void setNumAsignaturas(int numAsignaturas) { this.numAsignaturas = numAsignaturas; }
}
