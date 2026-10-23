package es.arevilla.universidad.etiquetas;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Etiqueta propia &lt;u:fecha/&gt;: fecha y hora del servidor en formato español.
 * Uso: &lt;%@ taglib prefix="u" uri="https://arevilla.es/tags/universidad" %&gt;
 */
public class FechaTag extends SimpleTagSupport {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm", Locale.forLanguageTag("es-ES"));

    private String zona = "Europe/Madrid";

    public void setZona(String zona) {
        this.zona = zona;
    }

    @Override
    public void doTag() throws JspException, IOException {
        try {
            getJspContext().getOut().print(LocalDateTime.now(ZoneId.of(zona)).format(FORMATO));
        } catch (java.time.DateTimeException e) {
            throw new JspException("Zona horaria no válida: " + zona, e);
        }
    }
}
