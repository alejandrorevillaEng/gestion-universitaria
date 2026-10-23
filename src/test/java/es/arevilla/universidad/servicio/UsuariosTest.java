package es.arevilla.universidad.servicio;

import es.arevilla.universidad.modelo.Rol;
import es.arevilla.universidad.modelo.Usuario;
import es.arevilla.universidad.seguridad.Contrasenas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuariosTest {

    private Servicios s;
    private int idAdmin;

    @BeforeEach
    void preparar() {
        s = BaseDatosPrueba.nueva();
        idAdmin = s.usuarios().autenticar("admin", "admin").orElseThrow().getId();
    }

    @AfterEach
    void cerrar() {
        s.close();
    }

    private int estado(Runnable accion) {
        return assertThrows(ErrorNegocio.class, accion::run).getEstadoHttp();
    }

    @Test
    @DisplayName("El script crea el admin/admin que pide el enunciado, con la contraseña hasheada")
    void adminPorDefecto() {
        Usuario admin = s.usuarios().buscar(idAdmin);
        assertTrue(admin.esAdmin());
        assertTrue(admin.getHashContrasena().startsWith("pbkdf2_sha256$600000$"));
        assertFalse(s.usuarios().autenticar("admin", "Admin").isPresent());
    }

    @Test
    @DisplayName("El autorregistro siempre crea usuarios normales y guarda solo el hash")
    void registro() {
        Usuario u = s.usuarios().registrar("lucia", "una-clave-larga");
        assertEquals(Rol.USUARIO, u.getRol());
        String hash = s.usuarios().buscar(u.getId()).getHashContrasena();
        assertFalse(hash.contains("una-clave-larga"));
        assertTrue(s.usuarios().autenticar("LUCIA", "una-clave-larga").isPresent());
    }

    @Test
    @DisplayName("Registro: nombre vacío, nulo, con caracteres raros o repetido; contraseña corta")
    void registroInvalido() {
        assertEquals(400, estado(() -> s.usuarios().registrar(null, "contraseña-larga")));
        assertEquals(400, estado(() -> s.usuarios().registrar("  ", "contraseña-larga")));
        assertEquals(400, estado(() -> s.usuarios().registrar("<script>", "contraseña-larga")));
        assertEquals(400, estado(() -> s.usuarios().registrar("pepe", "corta")));
        assertEquals(409, estado(() -> s.usuarios().registrar("Admin", "contraseña-larga")));
    }

    @Test
    @DisplayName("Regresión: un registro sin nombre no rompe el login de los demás")
    void registroSinNombreNoRompeLogin() {
        /* En la versión original, POST /registro sin parámetros creaba un usuario con nombre null
           y login() lanzaba NullPointerException al recorrer los usuarios. */
        assertThrows(ErrorNegocio.class, () -> s.usuarios().registrar(null, null));
        assertTrue(s.usuarios().autenticar("admin", "admin").isPresent());
        assertFalse(s.usuarios().autenticar(null, null).isPresent());
    }

    @Test
    @DisplayName("Tras varios fallos seguidos el login se bloquea un rato")
    void bloqueoPorFuerzaBruta() {
        for (int i = 0; i < 3; i++) assertFalse(s.usuarios().autenticar("admin", "mal" + i).isPresent());
        assertEquals(403, estado(() -> s.usuarios().autenticar("admin", "admin")));
    }

    @Test
    @DisplayName("Siempre queda al menos un administrador y nadie se borra a sí mismo")
    void ultimoAdmin() {
        assertEquals(409, estado(() -> s.usuarios().eliminar(idAdmin, idAdmin)));
        assertEquals(409, estado(() -> s.usuarios().actualizar(idAdmin, "admin", null, Rol.USUARIO, idAdmin)));

        Usuario otro = s.usuarios().crear("segundo", "otra-clave-larga", Rol.ADMIN);
        s.usuarios().actualizar(idAdmin, "admin", null, Rol.USUARIO, otro.getId());
        assertEquals(409, estado(() -> s.usuarios().eliminar(otro.getId(), idAdmin)));
    }

    @Test
    @DisplayName("Editar sin contraseña la conserva; con contraseña la cambia")
    void editarContrasena() {
        Usuario u = s.usuarios().registrar("mario", "primera-clave");
        s.usuarios().actualizar(u.getId(), "mario", "", Rol.USUARIO, idAdmin);
        assertTrue(s.usuarios().autenticar("mario", "primera-clave").isPresent());
        s.usuarios().actualizar(u.getId(), "mario", "segunda-clave", Rol.USUARIO, idAdmin);
        assertTrue(s.usuarios().autenticar("mario", "segunda-clave").isPresent());
    }

    @Test
    @DisplayName("PBKDF2: sal aleatoria, verificación correcta y formato inválido rechazado")
    void contrasenas() {
        String a = Contrasenas.hash("secreto123");
        String b = Contrasenas.hash("secreto123");
        assertNotEquals(a, b);
        assertTrue(Contrasenas.verificar("secreto123", a));
        assertFalse(Contrasenas.verificar("secreto124", a));
        assertFalse(Contrasenas.verificar("secreto123", "texto-plano"));
        assertFalse(Contrasenas.verificar("secreto123", "pbkdf2_sha256$x$y$z"));
        assertFalse(Contrasenas.verificar(null, a));
    }
}
