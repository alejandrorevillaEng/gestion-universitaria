package es.arevilla.universidad.modelo;

public class Aula {
    private int id;
    private String nombre;
    private String edificio;
    private int capacidad;
    private int numAsignaturas;

    public Aula() {
    }

    public Aula(int id, String nombre, String edificio, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.edificio = edificio;
        this.capacidad = capacidad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEdificio() { return edificio; }
    public void setEdificio(String edificio) { this.edificio = edificio; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    /** Solo lectura: asignaturas que se imparten en el aula. */
    public int getNumAsignaturas() { return numAsignaturas; }
    public void setNumAsignaturas(int numAsignaturas) { this.numAsignaturas = numAsignaturas; }
}
