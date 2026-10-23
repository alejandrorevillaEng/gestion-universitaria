package es.arevilla.universidad.web;

import es.arevilla.universidad.config.Configuracion;
import es.arevilla.universidad.datos.BaseDatos;
import es.arevilla.universidad.seguridad.LimiteIntentos;
import es.arevilla.universidad.servicio.Servicios;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Logger;

/** Al arrancar: conecta con la base de datos, crea el esquema y deja los servicios en el ServletContext. */
@WebListener
public class InicioAplicacion implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(InicioAplicacion.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        Configuracion config = Configuracion.desdeEntorno();
        Servicios servicios = Servicios.crear(BaseDatos.crear(config), LimiteIntentos.porDefecto());
        servicios.prepararBaseDatos(!"false".equalsIgnoreCase(System.getenv("DATOS_EJEMPLO")));
        if (config.contrasenaAdmin() != null) {
            servicios.usuarios().fijarContrasenaAdmin(config.contrasenaAdmin());
            LOG.info("Contraseña del administrador tomada de ADMIN_PASSWORD");
        } else {
            LOG.warning("Se usa la cuenta por defecto admin/admin. Define ADMIN_PASSWORD fuera de desarrollo.");
        }
        evento.getServletContext().setAttribute(Servicios.ATRIBUTO, servicios);
        LOG.info(() -> "Aplicación lista. Base de datos: " + ocultarCredenciales(config.urlBaseDatos()));
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        Object servicios = evento.getServletContext().getAttribute(Servicios.ATRIBUTO);
        if (servicios instanceof Servicios s) s.close();
    }

    private static String ocultarCredenciales(String url) {
        return url.replaceAll("(?i)(password|pwd)=[^&;]*", "$1=***");
    }
}
