package es.arevilla.universidad.servicio;

/** Escritura segura de campos CSV (separador ';', el que abre Excel en español). */
public final class Csv {

    private Csv() {
    }

    /**
     * Entrecomilla el campo si hace falta y neutraliza la inyección de fórmulas:
     * un valor que empieza por = + - @ (o tabulador/retorno) se ejecutaría como
     * fórmula al abrirlo en Excel, así que se le antepone un apóstrofo.
     */
    public static String campo(String valor) {
        if (valor == null) return "";
        String v = valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
