package es.arevilla.universidad.modelo;

public class Alumno {
    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private int numAsignaturas;

    public Alumno() {
    }

    public Alumno(int id, String nombre, String apellidos, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** Solo lectura: asignaturas en las que está matriculado. */
    public int getNumAsignaturas() { return numAsignaturas; }
    public void setNumAsignaturas(int numAsignaturas) { this.numAsignaturas = numAsignaturas; }
}
