package es.arevilla.universidad.servicio;

import es.arevilla.universidad.config.Configuracion;
import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.seguridad.LimiteIntentos;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/** Servicios sobre una base de datos H2 nueva y vacía (solo el esquema y el admin). */
final class BaseDatosPrueba {

    private BaseDatosPrueba() {
    }

    static Servicios nueva() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
        Servicios s = Servicios.crear(BaseDatos.crear(new Configuracion(url, "sa", "", null)),
                new LimiteIntentos(3, Duration.ofMinutes(5), Clock.systemUTC()));
        s.prepararBaseDatos(false);
        return s;
    }
}
