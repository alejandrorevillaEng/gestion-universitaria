package es.arevilla.universidad.datos;

import java.sql.SQLException;

/** Error inesperado de la base de datos. Se registra y al usuario se le muestra un error genérico. */
public class ErrorBaseDatos extends RuntimeException {
    public ErrorBaseDatos(SQLException causa) {
        super("Error de base de datos", causa);
    }
}
