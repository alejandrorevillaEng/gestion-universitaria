package es.arevilla.universidad.integracion;

import es.arevilla.universidad.ServidorDesarrollo;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La aplicación completa (filtros, servlets, JSP y Jersey) en un Tomcat
 * embebido, probada por HTTP como lo haría un navegador o un atacante.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AplicacionIntegracionTest {

    private Tomcat tomcat;
    private String base;

    @BeforeAll
    void arrancar() throws Exception {
        System.setProperty("db.url", "jdbc:h2:mem:integracion;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        tomcat = ServidorDesarrollo.arrancar(0);
        base = "http://localhost:" + tomcat.getConnector().getLocalPort();
    }

    @AfterAll
    void parar() throws Exception {
        tomcat.stop();
        tomcat.destroy();
        System.clearProperty("db.url");
    }

    /** Un navegador: guarda cookies y no sigue redirecciones, para poder comprobarlas. */
    private final class Navegador {
        final CookieManager cookies = new CookieManager();
        final HttpClient cliente = HttpClient.newBuilder().cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER).build();

        HttpResponse<String> get(String ruta) throws Exception {
            return cliente.send(HttpRequest.newBuilder(URI.create(base + ruta)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<String> post(String ruta, Map<String, String> campos) throws Exception {
            String cuerpo = campos.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            return cliente.send(HttpRequest.newBuilder(URI.create(base + ruta))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<String> api(String metodo, String ruta, String json, String token) throws Exception {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + "/api" + ruta))
                    .header("Accept", "application/json")
                    .method(metodo, json == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(json));
            if (json != null) b.header("Content-Type", "application/json");
            if (token != null) b.header("X-CSRF-Token", token);
            return cliente.send(b.build(), HttpResponse.BodyHandlers.ofString());
        }

        /** Token CSRF de la página (meta csrf-token). */
        String token(String ruta) throws Exception {
            Matcher m = Pattern.compile("name=\"csrf-token\" content=\"([^\"]+)\"").matcher(get(ruta).body());
            assertTrue(m.find(), "La página " + ruta + " no tiene token CSRF");
            return m.group(1);
        }

        String sesion() {
            return cookies.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("JSESSIONID")).map(Object::toString).findFirst().orElse("");
        }

        void entrar(String usuario, String contrasena) throws Exception {
            String token = token("/login");
            HttpResponse<String> r = post("/login", campos("_csrf", token, "nombre", usuario, "contrasena", contrasena));
            assertEquals(302, r.statusCode(), "El login de " + usuario + " debería redirigir");
        }
    }

    private static Map<String, String> campos(String... pares) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) m.put(pares[i], pares[i + 1]);
        return m;
    }

    private static String ubicacion(HttpResponse<?> r) {
        return r.headers().firstValue("Location").orElse("");
    }

    private static String idDe(String json) {
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(json);
        assertTrue(m.find(), json);
        return m.group(1);
    }

    private static String tokenApi(HttpResponse<String> login) {
        Matcher m = Pattern.compile("\"csrfToken\":\"([^\"]+)\"").matcher(login.body());
        assertTrue(m.find(), login.body());
        return m.group(1);
    }

    @Test
    @DisplayName("Sin sesión, las páginas redirigen al login y la API responde 401")
    void sinSesion() throws Exception {
        Navegador n = new Navegador();
        assertTrue(ubicacion(n.get("/asignaturas")).endsWith("/login"));
        HttpResponse<String> api = n.api("GET", "/titulaciones", null, null);
        assertEquals(401, api.statusCode());
        assertTrue(api.body().contains("\"error\""));
        assertEquals(200, n.get("/css/estilos.css").statusCode());
    }

    @Test
    @DisplayName("Regresión: no se puede saltar el login con parámetros de ruta ni rutas de ficheros estáticos")
    void saltoDelFiltro() throws Exception {
        Navegador n = new Navegador();
        for (String ruta : new String[] {"/titulaciones;x.css", "/titulaciones;.js", "/usuarios;/rest/",
                "/css/../titulaciones", "/titulaciones/x.css"}) {
            HttpResponse<String> r = n.get(ruta);
            assertNotEquals(200, r.statusCode(), ruta + " no debería ser accesible sin sesión");
            assertFalse(r.body().contains("Nueva titulación"), ruta);
        }
        assertEquals(401, n.api("POST", "/titulaciones", "{\"nombre\":\"Hack\"}", null).statusCode());
    }

    @Test
    @DisplayName("El login cambia el id de sesión (evita la fijación de sesión)")
    void fijacionDeSesion() throws Exception {
        Navegador n = new Navegador();
        n.get("/login");
        String antes = n.sesion();
        n.entrar("admin", "admin");
        assertFalse(antes.isEmpty());
        assertNotEquals(antes, n.sesion());
        assertEquals(200, n.get("/asignaturas").statusCode());
    }

    @Test
    @DisplayName("Credenciales incorrectas: 401 y mensaje genérico")
    void loginIncorrecto() throws Exception {
        Navegador n = new Navegador();
        String token = n.token("/login");
        HttpResponse<String> r = n.post("/login", campos("_csrf", token, "nombre", "nadie", "contrasena", "x"));
        assertEquals(401, r.statusCode());
        assertTrue(r.body().contains("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Un POST sin token CSRF se rechaza (web y API)")
    void csrf() throws Exception {
        Navegador n = new Navegador();
        n.entrar("admin", "admin");
        HttpResponse<String> r = n.post("/titulaciones", campos("nombre", "Sin token"));
        assertEquals(403, r.statusCode());
        assertEquals(403, n.api("POST", "/titulaciones", "{\"nombre\":\"Sin token\"}", null).statusCode());
        assertEquals(403, n.api("POST", "/titulaciones", "{\"nombre\":\"Token falso\"}", "falso").statusCode());
    }

    @Test
    @DisplayName("Regresión XSS: el HTML que mete un usuario se muestra escapado")
    void xssAlmacenado() throws Exception {
        Navegador n = new Navegador();
        n.entrar("admin", "admin");
        String carga = "<script>alert('xss')</script>";
        String token = n.token("/titulaciones?accion=nuevo");
        assertEquals(302, n.post("/titulaciones", campos("_csrf", token, "nombre", carga)).statusCode());

        String html = n.get("/titulaciones").body();
        assertFalse(html.contains(carga));
        assertTrue(html.contains("&lt;script&gt;alert(&#039;xss&#039;)&lt;/script&gt;"));
    }

    @Test
    @DisplayName("Las cabeceras de seguridad están en todas las respuestas")
    void cabeceras() throws Exception {
        HttpResponse<String> r = new Navegador().get("/login");
        assertTrue(r.headers().firstValue("Content-Security-Policy").orElse("").contains("script-src 'self'"));
        assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertEquals("DENY", r.headers().firstValue("X-Frame-Options").orElse(""));
        String cookie = r.headers().allValues("Set-Cookie").toString();
        assertTrue(cookie.contains("HttpOnly") && cookie.contains("SameSite=Lax"), cookie);
    }

    @Test
    @DisplayName("Un usuario normal no entra en la gestión de usuarios ni en las estadísticas")
    void permisos() throws Exception {
        Navegador n = new Navegador();
        String token = n.token("/registro");
        HttpResponse<String> r = n.post("/registro", campos("_csrf", token, "nombre", "normal",
                "contrasena", "clave-normal-1", "repetir", "clave-normal-1"));
        assertEquals(302, r.statusCode());
        n.entrar("normal", "clave-normal-1");

        assertEquals(200, n.get("/asignaturas").statusCode());
        assertEquals(403, n.get("/usuarios").statusCode());
        assertEquals(403, n.get("/usuarios;x.css").statusCode());
        assertEquals(403, n.get("/estadisticas").statusCode());
        assertEquals(403, n.api("GET", "/usuarios", null, null).statusCode());
        assertFalse(n.get("/asignaturas").body().contains("/usuarios\""));
    }

    @Test
    @DisplayName("Errores controlados: 400 y 404 con página propia, sin trazas de Java")
    void erroresSinTrazas() throws Exception {
        Navegador n = new Navegador();
        n.entrar("admin", "admin");
        HttpResponse<String> mal = n.get("/asignaturas?accion=ver&id=abc");
        assertEquals(400, mal.statusCode());
        assertFalse(mal.body().contains("Exception"));
        assertTrue(mal.body().contains("debe ser un número"));
        assertEquals(404, n.get("/asignaturas?accion=ver&id=99999").statusCode());
        assertEquals(400, n.api("POST", "/titulaciones", "{esto no es json", n.token("/asignaturas")).statusCode());
    }

    @Test
    @DisplayName("Todas las pantallas se pintan sin errores")
    void pantallas() throws Exception {
        Navegador n = new Navegador();
        n.entrar("admin", "admin");
        for (String ruta : new String[] {"/asignaturas", "/asignaturas?accion=nuevo", "/asignaturas?accion=ver&id=1",
                "/asignaturas?accion=editar&id=1", "/titulaciones", "/titulaciones?accion=ver&id=1",
                "/titulaciones?accion=nuevo", "/profesores", "/profesores?accion=editar&id=1", "/alumnos",
                "/alumnos?accion=ver&id=1", "/alumnos?accion=nuevo", "/usuarios", "/usuarios?accion=editar&id=1",
                "/estadisticas", "/panel-api", "/"}) {
            HttpResponse<String> r = n.get(ruta);
            assertEquals(200, r.statusCode(), ruta);
            assertTrue(r.body().contains("</html>"), ruta);
        }
        HttpResponse<String> inicio = n.get("/");
        assertEquals(200, inicio.statusCode());
        assertTrue(inicio.body().contains("Hola, admin"));
    }

    @Test
    @DisplayName("API REST: CRUD, asignación de profesor, matrículas hasta llenar y CSV")
    void apiCompleta() throws Exception {
        Navegador n = new Navegador();
        HttpResponse<String> login = n.api("POST", "/sesion", "{\"usuario\":\"admin\",\"contrasena\":\"admin\"}", null);
        assertEquals(200, login.statusCode());
        String t = tokenApi(login);

        HttpResponse<String> tit = n.api("POST", "/titulaciones", "{\"nombre\":\"Grado API\"}", t);
        assertEquals(201, tit.statusCode());
        assertTrue(tit.headers().firstValue("Location").orElse("").contains("/api/titulaciones/"));
        String idTit = idDe(tit.body());

        String idProf = idDe(n.api("POST", "/profesores", "{\"nombre\":\"Profe API\"}", t).body());
        HttpResponse<String> asig = n.api("POST", "/asignaturas",
                "{\"nombre\":\"Asignatura API\",\"creditos\":6,\"capacidad\":2,\"idTitulacion\":" + idTit + "}", t);
        assertEquals(201, asig.statusCode(), asig.body());
        String idAsig = idDe(asig.body());

        HttpResponse<String> asignada = n.api("PUT", "/asignaturas/" + idAsig + "/profesor/" + idProf, null, t);
        assertTrue(asignada.body().contains("\"nombreProfesor\":\"Profe API\""), asignada.body());

        String[] alumnos = new String[3];
        for (int i = 0; i < 3; i++) {
            alumnos[i] = idDe(n.api("POST", "/alumnos", "{\"nombre\":\"A" + i + "\",\"apellidos\":\"Api\","
                    + "\"email\":\"api" + i + "@ejemplo.es\"}", t).body());
        }
        assertEquals(204, n.api("PUT", "/asignaturas/" + idAsig + "/alumnos/" + alumnos[0], null, t).statusCode());
        assertEquals(204, n.api("PUT", "/asignaturas/" + idAsig + "/alumnos/" + alumnos[1], null, t).statusCode());
        HttpResponse<String> llena = n.api("PUT", "/asignaturas/" + idAsig + "/alumnos/" + alumnos[2], null, t);
        assertEquals(409, llena.statusCode());
        assertTrue(llena.body().contains("completa"));

        assertEquals(409, n.api("DELETE", "/asignaturas/" + idAsig, null, t).statusCode());
        assertEquals(409, n.api("DELETE", "/titulaciones/" + idTit, null, t).statusCode());

        HttpResponse<String> csv = n.get("/api/asignaturas/" + idAsig + "/alumnos.csv");
        assertTrue(csv.headers().firstValue("Content-Type").orElse("").startsWith("text/csv"));
        assertTrue(csv.body().contains("Api;A0;api0@ejemplo.es"), csv.body());

        assertEquals(400, n.api("POST", "/asignaturas", "{\"nombre\":\"Sin titulación\",\"capacidad\":5}", t).statusCode());
        assertEquals(404, n.api("GET", "/asignaturas/99999", null, t).statusCode());
        assertEquals(404, n.api("GET", "/ruta-que-no-existe", null, t).statusCode());

        HttpResponse<String> usuarios = n.api("GET", "/usuarios", null, t);
        assertEquals(200, usuarios.statusCode());
        assertFalse(usuarios.body().contains("pbkdf2"), "El hash nunca debe salir por la API");
    }
}
