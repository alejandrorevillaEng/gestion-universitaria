package es.arevilla.universidad.config;

/**
 * Configuración por variables de entorno (o por propiedades del sistema con el
 * mismo nombre en minúsculas y con puntos: DB_URL -> db.url). Sin nada
 * configurado arranca con una base de datos H2 en memoria, que basta para
 * probar la aplicación.
 */
public record Configuracion(String urlBaseDatos, String usuarioBaseDatos, String contrasenaBaseDatos,
                            String contrasenaAdmin) {

    public static final String URL_H2_MEMORIA =
            "jdbc:h2:mem:universidad;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE";

    public static Configuracion desdeEntorno() {
        return new Configuracion(
                leer("DB_URL", URL_H2_MEMORIA),
                leer("DB_USER", "sa"),
                leer("DB_PASSWORD", ""),
                leer("ADMIN_PASSWORD", null));
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getProperty(variable.toLowerCase().replace('_', '.'));
        if (valor == null || valor.isBlank()) valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
