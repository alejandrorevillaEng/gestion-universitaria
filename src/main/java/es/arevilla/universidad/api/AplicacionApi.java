package es.arevilla.universidad.api;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/** API REST bajo /api. Los recursos se registran a mano para no depender del escaneo de clases. */
@ApplicationPath("/api")
public final class AplicacionApi extends ResourceConfig {

    public AplicacionApi() {
        register(SesionRecurso.class);
        register(TitulacionesRecurso.class);
        register(ProfesoresRecurso.class);
        register(AsignaturasRecurso.class);
        register(AlumnosRecurso.class);
        register(AulasRecurso.class);
        register(SaludRecurso.class);
        register(UsuariosRecurso.class);
        register(EstadisticasRecurso.class);
        register(MapeadoresErrores.Negocio.class);
        register(MapeadoresErrores.Web.class);
        register(MapeadoresErrores.Json.class);
        register(MapeadoresErrores.Lectura.class);
        register(MapeadoresErrores.Inesperado.class);
    }
}
