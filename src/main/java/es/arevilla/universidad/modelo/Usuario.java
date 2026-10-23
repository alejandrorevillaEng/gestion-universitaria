package es.arevilla.universidad.modelo;

import jakarta.json.bind.annotation.JsonbTransient;

public class Usuario implements java.io.Serializable {
    private int id;
    private String nombre;
    private Rol rol = Rol.USUARIO;
    private String hashContrasena;

    public Usuario() {
    }

    public Usuario(int id, String nombre, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    /** El hash nunca sale en el JSON de la API. */
    @JsonbTransient
    public String getHashContrasena() { return hashContrasena; }
    @JsonbTransient
    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
}
