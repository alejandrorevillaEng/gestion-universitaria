# Gestión universitaria

Aplicación web en Java (Jakarta EE) para gestionar titulaciones, asignaturas, profesores, alumnos y matrículas, con una interfaz MVC hecha con Servlets y JSP y una API REST con Jersey que trabaja sobre los mismos datos.

[![CI](https://github.com/alejandrorevillaEng/gestion-universitaria/actions/workflows/ci.yml/badge.svg)](https://github.com/alejandrorevillaEng/gestion-universitaria/actions/workflows/ci.yml)
[![Seguridad](https://github.com/alejandrorevillaEng/gestion-universitaria/actions/workflows/seguridad.yml/badge.svg)](https://github.com/alejandrorevillaEng/gestion-universitaria/actions/workflows/seguridad.yml)
[![Licencia: MIT](https://img.shields.io/badge/licencia-MIT-blue.svg)](LICENSE)

![Panel de inicio](docs/capturas/inicio.jpg)

## Por qué existe

Nació como la práctica de Desarrollo de Aplicaciones Distribuidas II (Grado en Ingeniería Informática, UCAM, curso 2025-26). La versión entregada cumplía el enunciado, pero guardaba todo en memoria, la API REST era pública y no tenía tests. Después la he rehecho para que se parezca a lo que haría en un proyecto real: base de datos, seguridad revisada a fondo, tests de integración y despliegue con Docker. También he añadido lo que el enunciado pedía para las convocatorias siguientes (alumnos, matrículas, exportar a CSV, panel de estadísticas y aulas).

## Qué hace

- Login, autorregistro y una zona de administración (gestión de usuarios y estadísticas) a la que solo entra el administrador.
- Gestión completa de titulaciones, asignaturas, profesores y alumnos.
- Asignación de un profesor a cada asignatura y matrícula de alumnos respetando la capacidad máxima.
- No deja borrar lo que dejaría datos colgando: una asignatura con alumnos, una titulación con asignaturas o un profesor que imparte clase.
- Aulas con su capacidad: una asignatura solo se puede poner en un aula donde quepan todos sus alumnos, y ninguna de las dos puede cambiar de tamaño si deja de cumplirse.
- Búsqueda y paginación en los listados de asignaturas y alumnos, también desde la API.
- Exporta los matriculados de cada asignatura a CSV.
- Panel de estadísticas: total de alumnos, asignaturas por titulación y ocupación de cada asignatura.
- API REST con las mismas operaciones, documentada en OpenAPI ([guía](docs/api.md) y [especificación](docs/openapi.yaml)), y una página que la usa desde JavaScript.
- Interfaz propia, sin frameworks de CSS: tipografía Geist servida desde la app, iconos SVG propios, modo oscuro automático, diseño adaptable al móvil y animaciones que se desactivan si el sistema pide reducir el movimiento.

| Acceso | Ficha de una asignatura (modo oscuro) |
|---|---|
| ![Pantalla de acceso](docs/capturas/login.jpg) | ![Ficha con sus matriculados en modo oscuro](docs/capturas/matricula-oscuro.jpg) |
| Búsqueda en el listado | Aulas asignadas a una asignatura |
| ![Listado de asignaturas filtrado](docs/capturas/busqueda.jpg) | ![Ficha de asignatura con su aula](docs/capturas/aulas.jpg) |

## Stack y decisiones técnicas

| Pieza | Tecnología | Por qué |
|---|---|---|
| Web | Servlets 6.1 y JSP con JSTL | Es lo que pedía la asignatura (MVC sin framework). Con JSTL todo se pinta escapado y las vistas no llevan Java. |
| API | Jersey 4 (Jakarta REST) con JSON-B | La implementación de referencia de JAX-RS. Los recursos llaman a los mismos servicios que los servlets, así que las reglas de negocio están en un solo sitio. |
| Datos | JDBC con HikariCP, MySQL 8.4 en Docker y H2 en desarrollo | Con SQL a mano se ve cada consulta, y todas van parametrizadas. H2 en modo MySQL permite arrancar la aplicación y los tests sin instalar nada. |
| Esquema | Flyway | Las migraciones se aplican solas al arrancar. Añadir las aulas fue una migración nueva, sin tocar la base de datos a mano. |
| Servidor | Tomcat 11 | En producción, la imagen oficial. En desarrollo y en los tests, Tomcat embebido. |
| Tests | JUnit 6, Tomcat embebido y Testcontainers | Los tests de integración levantan la aplicación entera y la prueban por HTTP. Lo que depende del motor de base de datos se prueba además contra un MySQL de verdad. |

## Arquitectura

```mermaid
flowchart LR
  Navegador -->|HTML| Filtros
  JS[Panel API / curl] -->|JSON| Filtros
  Filtros["Filtros: cabeceras, auditoría, autenticación, CSRF"] --> Servlets
  Filtros --> Jersey[Recursos REST]
  Servlets --> JSP[Vistas JSP]
  Servlets --> Servicios
  Jersey --> Servicios
  Servicios["Servicios: validación y reglas"] --> BD[(MySQL / H2)]
```

Los servlets (`web/`) y los recursos REST (`api/`) no tienen lógica: leen la petición, llaman a un servicio (`servicio/`) y devuelven una vista o un JSON. Las reglas (capacidad, borrados, permisos sobre usuarios) y la validación están en los servicios, y cada operación va en una transacción. La matrícula bloquea la fila de la asignatura (`SELECT ... FOR UPDATE`) para que dos matrículas a la vez no ocupen la misma última plaza.

Las decisiones de diseño, con su contexto y lo que se pierde con cada una, están en [docs/arquitectura.md](docs/arquitectura.md).

## Cómo ejecutarlo

Requisitos: Java 21. No hace falta instalar Maven ni Tomcat (en Windows, usa `mvnw.cmd` en lugar de `./mvnw`).

```bash
git clone https://github.com/alejandrorevillaEng/gestion-universitaria.git
cd gestion-universitaria
./mvnw test-compile exec:exec@dev
```

Abre http://localhost:8080 y entra con `admin` / `admin`. Arranca con una base de datos H2 en memoria y datos de ejemplo inventados.

Con MySQL y la imagen de producción (Docker):

```bash
cp .env.example .env    # pon contraseñas en DB_PASSWORD y ADMIN_PASSWORD
docker compose up --build --wait
```

Las tablas las crea Flyway al arrancar a partir de [`db/migration`](src/main/resources/db/migration). Con MySQL la aplicación no arranca sin `ADMIN_PASSWORD`, así que la cuenta `admin/admin` solo existe en desarrollo. `GET /api/salud` dice si la aplicación llega a la base de datos, y la imagen la usa como healthcheck.

## Tests

```bash
./mvnw verify          # 42 tests contra H2 y Tomcat embebido
./mvnw -Pmysql test    # 4 tests contra MySQL 8.4 en Docker
```

- Reglas de negocio: capacidad, matrículas concurrentes, aulas, borrados con dependencias, búsqueda con `%` y `_`, paginación y validación.
- Integración: la aplicación entera en un Tomcat embebido, atacada por HTTP como lo haría un navegador o un atacante (salto del login, CSRF, XSS, permisos, errores sin trazas).
- Contrato: un test compara las rutas de la API con las de `docs/openapi.yaml`.
- MySQL: migraciones, comodines en `LIKE` y bloqueo de la matrícula sobre el motor de producción.

La build falla si la cobertura baja del 75 % de líneas o del 65 % de ramas (ahora está en el 78 % y el 69 %). El informe queda en `target/site/jacoco/`.

## Seguridad

Al revisar la versión entregada encontré varios fallos. Cada uno tiene ahora un test que falla si vuelve a aparecer:

- La API REST no pedía sesión. Ahora pasa por el mismo control de acceso que la web, y la gestión de usuarios es solo para administradores.
- El filtro de login miraba `getRequestURI()` con `endsWith(".css")`, así que una URL como `/titulaciones;x.css` se lo saltaba. Ahora usa la ruta ya normalizada por el contenedor.
- XSS almacenado: las JSP pintaban los datos sin escapar y el panel REST usaba `innerHTML`. Ahora todo pasa por JSTL o `textContent`, y una Content-Security-Policy sin scripts en línea hace de segunda barrera.
- Las contraseñas se guardaban en claro. Ahora van con PBKDF2-SHA256 (600.000 iteraciones), y el login se bloquea un rato tras varios fallos seguidos.
- Los borrados se hacían por GET y sin protección CSRF. Ahora van por POST con token, la cookie de sesión es HttpOnly y SameSite, y al entrar se genera un id de sesión nuevo.
- Un registro sin nombre podía romper el login de todos los usuarios (un `NullPointerException`). Ahora se valida la entrada y los errores devuelven 400 o 409 con una página propia, nunca una traza de Java.

Además, pensando en tenerla en producción:

- Con MySQL no arranca sin `ADMIN_PASSWORD`, para que nadie la despliegue con la contraseña por defecto.
- Cada cambio queda en un registro de auditoría (usuario, método, ruta, resultado e IP). Los valores se limpian de saltos de línea para que no se puedan inventar entradas.
- HSTS cuando la conexión es HTTPS, y la IP y el esquema reales detrás de un proxy.

En cada cambio se ejecutan gitleaks, semgrep y osv-scanner. Para informar de una vulnerabilidad: [SECURITY.md](SECURITY.md).

![Panel de estadísticas](docs/capturas/estadisticas.jpg)

## Próximos pasos

- Horarios: ahora un aula se asigna a una asignatura sin franja horaria, así que no se detectan solapes.
- Tokens de acceso para la API, para clientes que no manejan cookies.
- Una demo desplegada para probarla sin clonar el repo.

Los cambios de cada versión están en el [CHANGELOG](CHANGELOG.md), y cómo contribuir, en [CONTRIBUTING.md](CONTRIBUTING.md).

## Autor

Alejandro Revilla Blanco

## Licencia

[MIT](LICENSE)
