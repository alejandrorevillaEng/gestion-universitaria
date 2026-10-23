package es.arevilla.universidad.servicio;

import java.util.regex.Pattern;

/** Validaciones de entrada compartidas por la web y la API. */
public final class Validar {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validar() {
    }

    /** Texto obligatorio: sin espacios sobrantes, no vacío y con longitud máxima. */
    public static String obligatorio(String valor, String campo, int maximo) {
        String limpio = valor == null ? "" : valor.strip();
        if (limpio.isEmpty()) throw ErrorNegocio.validacion("El campo «" + campo + "» es obligatorio");
        if (limpio.length() > maximo) {
            throw ErrorNegocio.validacion("El campo «" + campo + "» admite como mucho " + maximo + " caracteres");
        }
        return limpio;
    }

    /** Texto opcional: null si viene vacío. */
    public static String opcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        return obligatorio(valor, campo, maximo);
    }

    public static int rango(int valor, String campo, int minimo, int maximo) {
        if (valor < minimo || valor > maximo) {
            throw ErrorNegocio.validacion("El campo «" + campo + "» debe estar entre " + minimo + " y " + maximo);
        }
        return valor;
    }

    public static String email(String valor) {
        String limpio = obligatorio(valor, "email", 160).toLowerCase();
        if (!EMAIL.matcher(limpio).matches()) throw ErrorNegocio.validacion("El email no tiene un formato válido");
        return limpio;
    }

    /** Convierte un parámetro de texto en entero o lanza un error de validación (400, no 500). */
    public static int entero(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw ErrorNegocio.validacion("Falta el campo «" + campo + "»");
        try {
            return Integer.parseInt(valor.strip());
        } catch (NumberFormatException e) {
            throw ErrorNegocio.validacion("El campo «" + campo + "» debe ser un número entero");
        }
    }

    /** Entero opcional: null si viene vacío o es 0 (la opción «sin asignar» de los formularios). */
    public static Integer enteroOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        int v = entero(valor, campo);
        return v == 0 ? null : v;
    }
}
