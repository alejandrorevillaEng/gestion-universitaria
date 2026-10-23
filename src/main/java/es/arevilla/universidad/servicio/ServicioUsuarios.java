package es.arevilla.universidad.servicio;

import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.datos.Jdbc;
import es.arevilla.universidad.modelo.Rol;
import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Contrasenas;
import es.arevilla.universidad.seguridad.LimiteIntentos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServicioUsuarios {

    public static final int LONGITUD_MINIMA_CONTRASENA = 8;

    /** Hash de una contraseña cualquiera: se verifica contra él cuando el usuario no existe,
     *  para que la respuesta tarde lo mismo y no delate qué usuarios hay. */
    private static final String HASH_SENUELO = Contrasenas.hash("contraseña-señuelo");

    private final BaseDatos db;
    private final LimiteIntentos limite;

    public ServicioUsuarios(BaseDatos db, LimiteIntentos limite) {
        this.db = db;
        this.limite = limite;
    }

    /**
     * Comprueba las credenciales. Tras varios fallos seguidos con el mismo
     * nombre, bloquea los intentos un rato.
     */
    public Optional<Usuario> autenticar(String nombre, String contrasena) {
        if (contrasena == null || contrasena.length() > 128 || nombre == null || nombre.length() > 50) {
            return Optional.empty();
        }
        String clave = nombre.strip().toLowerCase();
        if (limite.bloqueado(clave)) {
            throw ErrorNegocio.prohibido("Demasiados intentos fallidos. Espera unos minutos y vuelve a probar.");
        }
        Optional<Usuario> usuario = db.transaccion(c -> buscarPorNombre(c, nombre.strip()));
        String hash = usuario.map(Usuario::getHashContrasena).orElse(HASH_SENUELO);
        boolean correcta = Contrasenas.verificar(contrasena, hash) && usuario.isPresent();
        if (!correcta) {
            limite.fallo(clave);
            return Optional.empty();
        }
        limite.exito(clave);
        return usuario;
    }

    /** Autorregistro: siempre con rol USUARIO, nunca ADMIN. */
    public Usuario registrar(String nombre, String contrasena) {
        return crear(nombre, contrasena, Rol.USUARIO);
    }

    public List<Usuario> listar() {
        return db.transaccion(c -> Jdbc.lista(c, "SELECT * FROM usuario ORDER BY nombre", ServicioUsuarios::mapear));
    }

    public Usuario buscar(int id) {
        return db.transaccion(c -> Jdbc.uno(c, "SELECT * FROM usuario WHERE id = ?", ServicioUsuarios::mapear, id))
                .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el usuario " + id));
    }

    public Usuario crear(String nombre, String contrasena, Rol rol) {
        String n = validarNombre(nombre);
        validarContrasena(contrasena);
        Rol r = rol == null ? Rol.USUARIO : rol;
        String hash = Contrasenas.hash(contrasena);
        return db.transaccion(c -> {
            if (buscarPorNombre(c, n).isPresent()) throw ErrorNegocio.conflicto("Ese nombre de usuario ya existe");
            int id = Jdbc.insertar(c, "INSERT INTO usuario (nombre, hash_contrasena, rol) VALUES (?, ?, ?)",
                    n, hash, r.name());
            return new Usuario(id, n, r);
        });
    }

    /**
     * Cambia nombre y rol, y la contraseña solo si llega una nueva. Nadie puede
     * quitarse a sí mismo el rol de admin ni dejar el sistema sin administradores.
     */
    public Usuario actualizar(int id, String nombre, String nuevaContrasena, Rol rol, int idQuienEdita) {
        String n = validarNombre(nombre);
        Rol r = rol == null ? Rol.USUARIO : rol;
        boolean cambiaContrasena = nuevaContrasena != null && !nuevaContrasena.isEmpty();
        if (cambiaContrasena) validarContrasena(nuevaContrasena);
        String hash = cambiaContrasena ? Contrasenas.hash(nuevaContrasena) : null;
        return db.transaccion(c -> {
            Usuario actual = Jdbc.uno(c, "SELECT * FROM usuario WHERE id = ?", ServicioUsuarios::mapear, id)
                    .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el usuario " + id));
            Optional<Usuario> mismoNombre = buscarPorNombre(c, n);
            if (mismoNombre.isPresent() && mismoNombre.get().getId() != id) {
                throw ErrorNegocio.conflicto("Ese nombre de usuario ya existe");
            }
            if (actual.esAdmin() && r != Rol.ADMIN) {
                if (id == idQuienEdita) throw ErrorNegocio.conflicto("No puedes quitarte a ti mismo el rol de administrador");
                comprobarQueQuedaOtroAdmin(c);
            }
            Jdbc.actualizar(c, "UPDATE usuario SET nombre = ?, rol = ? WHERE id = ?", n, r.name(), id);
            if (hash != null) Jdbc.actualizar(c, "UPDATE usuario SET hash_contrasena = ? WHERE id = ?", hash, id);
            return new Usuario(id, n, r);
        });
    }

    public void eliminar(int id, int idQuienBorra) {
        if (id == idQuienBorra) throw ErrorNegocio.conflicto("No puedes borrar tu propio usuario");
        db.transaccion(c -> {
            Usuario u = Jdbc.uno(c, "SELECT * FROM usuario WHERE id = ?", ServicioUsuarios::mapear, id)
                    .orElseThrow(() -> ErrorNegocio.noEncontrado("No existe el usuario " + id));
            if (u.esAdmin()) comprobarQueQuedaOtroAdmin(c);
            Jdbc.actualizar(c, "DELETE FROM usuario WHERE id = ?", id);
            return null;
        });
    }

    /** Se usa al arrancar si se define ADMIN_PASSWORD: sustituye la contraseña por defecto. */
    public void fijarContrasenaAdmin(String contrasena) {
        validarContrasena(contrasena);
        String hash = Contrasenas.hash(contrasena);
        db.transaccion(c -> Jdbc.actualizar(c, "UPDATE usuario SET hash_contrasena = ? WHERE nombre = 'admin'", hash));
    }

    private static void comprobarQueQuedaOtroAdmin(Connection c) throws SQLException {
        if (Jdbc.entero(c, "SELECT COUNT(*) FROM usuario WHERE rol = 'ADMIN'") <= 1) {
            throw ErrorNegocio.conflicto("Tiene que quedar al menos un administrador");
        }
    }

    private static Optional<Usuario> buscarPorNombre(Connection c, String nombre) throws SQLException {
        return Jdbc.uno(c, "SELECT * FROM usuario WHERE LOWER(nombre) = LOWER(?)", ServicioUsuarios::mapear, nombre);
    }

    private static String validarNombre(String nombre) {
        String n = Validar.obligatorio(nombre, "usuario", 50);
        if (!n.matches("[\\p{L}\\p{N}._-]+")) {
            throw ErrorNegocio.validacion("El usuario solo puede tener letras, números, punto, guion y guion bajo");
        }
        return n;
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw ErrorNegocio.validacion("La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        if (contrasena.length() > 128) throw ErrorNegocio.validacion("La contraseña es demasiado larga");
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(rs.getInt("id"), rs.getString("nombre"), Rol.valueOf(rs.getString("rol")));
        u.setHashContrasena(rs.getString("hash_contrasena"));
        return u;
    }
}
