package es.arevilla.universidad.servicio;

/**
 * Error esperado de la lógica de negocio. Lleva un mensaje pensado para el
 * usuario y el código HTTP con el que lo devuelve la API.
 */
public class ErrorNegocio extends RuntimeException {

    private final int estadoHttp;

    protected ErrorNegocio(int estadoHttp, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }

    /** 400: datos de entrada incorrectos. */
    public static ErrorNegocio validacion(String mensaje) {
        return new ErrorNegocio(400, mensaje);
    }

    /** 404: el recurso no existe. */
    public static ErrorNegocio noEncontrado(String mensaje) {
        return new ErrorNegocio(404, mensaje);
    }

    /** 409: la operación choca con una regla de negocio (capacidad, borrados con dependencias...). */
    public static ErrorNegocio conflicto(String mensaje) {
        return new ErrorNegocio(409, mensaje);
    }

    /** 403: el usuario no tiene permiso. */
    public static ErrorNegocio prohibido(String mensaje) {
        return new ErrorNegocio(403, mensaje);
    }
}
