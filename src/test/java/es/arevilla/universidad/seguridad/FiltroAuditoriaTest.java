package es.arevilla.universidad.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltroAuditoriaTest {

    @Test
    @DisplayName("Un nombre con saltos de línea no puede inventarse entradas en el registro")
    void inyeccionEnElRegistro() {
        String falso = "pepe\nusuario=admin metodo=DELETE ruta=/usuarios estado=204";
        assertEquals("pepe_usuario=admin metodo=DELETE ruta=/usuarios estado=204", FiltroAuditoria.limpiar(falso));
        assertEquals("a__b_c", FiltroAuditoria.limpiar("a\r\nb\tc"));
        assertEquals("-", FiltroAuditoria.limpiar(null));
    }
}
