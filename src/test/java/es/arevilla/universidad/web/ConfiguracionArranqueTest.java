package es.arevilla.universidad.web;

import es.arevilla.universidad.config.Configuracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracionArranqueTest {

    private static final String MYSQL = "jdbc:mysql://db:3306/universidad";

    @Test
    @DisplayName("Con MySQL no se arranca sin ADMIN_PASSWORD")
    void mysqlSinContrasenaDeAdmin() {
        assertThrows(IllegalStateException.class,
                () -> InicioAplicacion.comprobarConfiguracion(new Configuracion(MYSQL, "app", "x", null)));
        assertThrows(IllegalStateException.class,
                () -> InicioAplicacion.comprobarConfiguracion(new Configuracion(MYSQL, "app", "x", "  ")));
    }

    @Test
    @DisplayName("Con ADMIN_PASSWORD, o con H2 en desarrollo, arranca")
    void arranquesValidos() {
        assertDoesNotThrow(() -> InicioAplicacion.comprobarConfiguracion(
                new Configuracion(MYSQL, "app", "x", "una-clave-larga-de-verdad")));
        assertDoesNotThrow(() -> InicioAplicacion.comprobarConfiguracion(
                new Configuracion(Configuracion.URL_H2_MEMORIA, "sa", "", null)));
    }
}
