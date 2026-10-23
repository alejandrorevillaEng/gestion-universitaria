package es.arevilla.universidad.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Utilidades mínimas sobre JDBC. Todas las consultas van parametrizadas. */
public final class Jdbc {

    @FunctionalInterface
    public interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    public static <T> List<T> lista(Connection c, String sql, Mapeador<T> m, Object... params)
            throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, false, params); ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(m.mapear(rs));
            return resultado;
        }
    }

    public static <T> Optional<T> uno(Connection c, String sql, Mapeador<T> m, Object... params)
            throws SQLException {
        List<T> l = lista(c, sql, m, params);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public static int entero(Connection c, String sql, Object... params) throws SQLException {
        return uno(c, sql, rs -> rs.getInt(1), params).orElse(0);
    }

    public static int actualizar(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, false, params)) {
            return ps.executeUpdate();
        }
    }

    /** INSERT que devuelve la clave generada. */
    public static int insertar(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = preparar(c, sql, true, params)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("El INSERT no devolvió ninguna clave");
                return rs.getInt(1);
            }
        }
    }

    public static Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
        int v = rs.getInt(columna);
        return rs.wasNull() ? null : v;
    }

    private static PreparedStatement preparar(Connection c, String sql, boolean claves, Object... params)
            throws SQLException {
        PreparedStatement ps = claves
                ? c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : c.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) ps.setNull(i + 1, Types.INTEGER);
                else ps.setObject(i + 1, params[i]);
            }
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
        return ps;
    }
}
