package es.arevilla.universidad.api;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La documentación de la API (docs/openapi.yaml) tiene exactamente los endpoints que existen en el código. */
class ContratoApiTest {

    private static final Map<Class<? extends java.lang.annotation.Annotation>, String> METODOS =
            Map.of(GET.class, "get", POST.class, "post", PUT.class, "put", DELETE.class, "delete");

    @Test
    @DisplayName("Cada endpoint de Jersey está en openapi.yaml y no sobra ninguno")
    void especificacionAlDia() throws IOException {
        Set<String> codigo = endpointsDelCodigo();
        assertTrue(codigo.size() > 40, "No se han encontrado los recursos: " + codigo);
        assertEquals(codigo, endpointsDocumentados());
    }

    private static Set<String> endpointsDelCodigo() {
        Set<String> endpoints = new TreeSet<>();
        for (Class<?> recurso : new AplicacionApi().getClasses()) {
            Path base = recurso.getAnnotation(Path.class);
            if (base == null) continue;
            for (Method m : recurso.getDeclaredMethods()) {
                Path sub = m.getAnnotation(Path.class);
                String ruta = base.value() + (sub == null ? "" : sub.value());
                METODOS.forEach((anotacion, verbo) -> {
                    if (m.isAnnotationPresent(anotacion)) endpoints.add(verbo + " " + ruta);
                });
            }
        }
        return endpoints;
    }

    /** Lectura mínima del YAML: rutas a dos espacios y verbos a cuatro, que es como está escrito. */
    private static Set<String> endpointsDocumentados() throws IOException {
        Pattern ruta = Pattern.compile("^  (/\\S+):\\s*$");
        Pattern verbo = Pattern.compile("^    (get|post|put|delete):\\s*$");
        Set<String> endpoints = new TreeSet<>();
        String actual = null;
        for (String linea : Files.readAllLines(java.nio.file.Path.of("docs", "openapi.yaml"), StandardCharsets.UTF_8)) {
            Matcher r = ruta.matcher(linea);
            Matcher v = verbo.matcher(linea);
            if (r.matches()) actual = r.group(1);
            else if (!linea.startsWith(" ") && !linea.isBlank()) actual = null;
            else if (actual != null && v.matches()) endpoints.add(v.group(1) + " " + actual);
        }
        return endpoints;
    }
}
