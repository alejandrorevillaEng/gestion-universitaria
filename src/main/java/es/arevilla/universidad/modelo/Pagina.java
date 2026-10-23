package es.arevilla.universidad.modelo;

import java.util.List;

/**
 * Un trozo de un listado: los elementos de la página y el total que coincide con la búsqueda.
 * El EL de Tomcat lee los records llamando a {@code nombre()}, así que los derivados siguen esa forma.
 */
public record Pagina<T>(List<T> elementos, int total, int pagina, int tamano) {

    public int totalPaginas() {
        return Math.max(1, (total + tamano - 1) / tamano);
    }

    public boolean hayAnterior() {
        return pagina > 1;
    }

    public boolean haySiguiente() {
        return pagina < totalPaginas();
    }
}
