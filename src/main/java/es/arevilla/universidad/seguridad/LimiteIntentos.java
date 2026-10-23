package es.arevilla.universidad.seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freno contra ataques de fuerza bruta al login: tras {@code maxFallos} fallos
 * seguidos con el mismo usuario, se rechazan los intentos durante {@code bloqueo}.
 * Vive en memoria; con varias instancias habría que llevarlo a la base de datos.
 */
public class LimiteIntentos {

    private record Estado(int fallos, Instant hasta) {
    }

    private final int maxFallos;
    private final Duration bloqueo;
    private final Clock reloj;
    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();

    public LimiteIntentos(int maxFallos, Duration bloqueo, Clock reloj) {
        this.maxFallos = maxFallos;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    public static LimiteIntentos porDefecto() {
        return new LimiteIntentos(5, Duration.ofMinutes(5), Clock.systemUTC());
    }

    public boolean bloqueado(String clave) {
        Estado e = estados.get(clave);
        return e != null && e.hasta() != null && reloj.instant().isBefore(e.hasta());
    }

    public void fallo(String clave) {
        estados.compute(clave, (k, e) -> {
            int fallos = (e == null || (e.hasta() != null && !reloj.instant().isBefore(e.hasta())))
                    ? 1 : e.fallos() + 1;
            Instant hasta = fallos >= maxFallos ? reloj.instant().plus(bloqueo) : null;
            return new Estado(fallos, hasta);
        });
    }

    public void exito(String clave) {
        estados.remove(clave);
    }
}
