package es.arevilla.universidad.seguridad;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Respuestas de error en JSON para los filtros (que se ejecutan antes de Jersey). */
public final class Respuestas {

    private Respuestas() {
    }

    public static void errorJson(HttpServletResponse resp, int estado, String mensaje) throws IOException {
        resp.setStatus(estado);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"error\":\"" + escaparJson(mensaje) + "\"}");
    }

    static String escaparJson(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char ch : texto.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.toString();
    }
}
