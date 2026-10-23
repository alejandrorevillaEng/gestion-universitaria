package es.arevilla.universidad.datos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import es.arevilla.universidad.config.Configuracion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Pool de conexiones y transacciones. Los servicios piden una conexión con
 * {@link #transaccion} y todo lo que hacen dentro se confirma o se deshace junto.
 */
public class BaseDatos implements AutoCloseable {

    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    private final DataSource origen;

    public BaseDatos(DataSource origen) {
        this.origen = origen;
    }

    public static BaseDatos crear(Configuracion config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.urlBaseDatos());
        /* En un Tomcat normal el driver va dentro del WAR y DriverManager no lo
         * encuentra solo ("No suitable driver"); hay que nombrar la clase. */
        hikari.setDriverClassName(claseDriver(config.urlBaseDatos()));
        hikari.setUsername(config.usuarioBaseDatos());
        hikari.setPassword(config.contrasenaBaseDatos());
        hikari.setMaximumPoolSize(10);
        hikari.setPoolName("universidad");
        return new BaseDatos(new HikariDataSource(hikari));
    }

    static String claseDriver(String url) {
        if (url.startsWith("jdbc:mysql:")) return "com.mysql.cj.jdbc.Driver";
        if (url.startsWith("jdbc:h2:")) return "org.h2.Driver";
        throw new IllegalArgumentException("Base de datos no soportada: solo MySQL y H2");
    }

    public <T> T transaccion(Trabajo<T> trabajo) {
        try (Connection c = origen.getConnection()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos(e);
        }
    }

    /** Ejecuta un script SQL del classpath (los datos de ejemplo). */
    public void ejecutarScript(String recurso) {
        String sql;
        try (InputStream in = BaseDatos.class.getResourceAsStream(recurso)) {
            if (in == null) throw new IllegalStateException("No existe el recurso " + recurso);
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        transaccion(c -> {
            try (Statement st = c.createStatement()) {
                for (String sentencia : separarSentencias(sql)) st.execute(sentencia);
            }
            return null;
        });
    }

    /** Quita los comentarios de línea y separa por el ';' que cierra cada sentencia. */
    static List<String> separarSentencias(String script) {
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\\R")) {
            if (!linea.strip().startsWith("--")) limpio.append(linea).append('\n');
        }
        List<String> sentencias = new ArrayList<>();
        for (String s : limpio.toString().split(";\\s*\\n")) {
            String sentencia = s.strip();
            if (sentencia.endsWith(";")) sentencia = sentencia.substring(0, sentencia.length() - 1);
            if (!sentencia.isBlank()) sentencias.add(sentencia);
        }
        return sentencias;
    }

    /** Aplica las migraciones pendientes de db/migration (V1__esquema.sql, V2__aulas.sql...). */
    public void migrar() {
        org.flywaydb.core.Flyway.configure()
                .dataSource(origen)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Override
    public void close() {
        if (origen instanceof HikariDataSource hikari) hikari.close();
    }
}
