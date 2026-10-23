package es.arevilla.universidad.servicio;

/** Parámetros de búsqueda y paginación, ya validados. */
public record Busqueda(String texto, int pagina, int tamano) {

    public static final int TAMANO_POR_DEFECTO = 20;
    public static final int TAMANO_MAXIMO = 100;

    public static Busqueda de(String texto, Integer pagina, Integer tamano) {
        String t = texto == null || texto.isBlank() ? null : Validar.obligatorio(texto, "búsqueda", 100);
        int p = pagina == null ? 1 : Validar.rango(pagina, "página", 1, 1_000_000);
        int tam = tamano == null ? TAMANO_POR_DEFECTO : Validar.rango(tamano, "tamaño de página", 1, TAMANO_MAXIMO);
        return new Busqueda(t, p, tam);
    }

    /**
     * Patrón para LIKE ... ESCAPE '!' con los comodines del usuario escapados: "50%" busca el
     * texto "50%". Se usa '!' y no la barra porque MySQL y H2 tratan la barra de forma distinta.
     */
    public String patron() {
        if (texto == null) return "%";
        String escapado = texto.toLowerCase().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escapado + "%";
    }

    public int desplazamiento() {
        return (pagina - 1) * tamano;
    }
}
