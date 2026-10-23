# Cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). Las versiones siguen [SemVer](https://semver.org/lang/es/).

## [1.1.0] - 2026-10-23

### Añadido
- Aulas y su relación con las asignaturas, que era lo que el enunciado dejaba para la convocatoria especial. Una asignatura solo se puede poner en un aula donde quepan todos sus alumnos, y ni el aula ni la asignatura pueden cambiar de capacidad si dejan de cumplirlo.
- Búsqueda y paginación en los listados de asignaturas y alumnos, en la web y en la API (`q`, `pagina`, `tam` y la cabecera `X-Total-Count`).
- Especificación OpenAPI 3.1 en `docs/openapi.yaml`, con un test que falla si el código y la especificación no tienen las mismas rutas.
- `GET /api/salud` y `HEALTHCHECK` en la imagen Docker.
- Registro de auditoría: quién hace cada cambio (método, ruta, estado e IP), con los saltos de línea neutralizados para que no se puedan falsear entradas.
- Migraciones con Flyway: la aplicación crea y actualiza el esquema al arrancar.
- Tests contra MySQL 8.4 real con Testcontainers (migraciones, búsqueda con comodines y matrículas concurrentes) en un job propio de CI.
- Mínimo de cobertura en la build (75 % de líneas, 65 % de ramas) y workflow de release que publica el WAR y la imagen.

### Cambiado
- Con MySQL la aplicación no arranca si falta `ADMIN_PASSWORD`, para que nunca quede en producción la cuenta `admin/admin`.
- HSTS cuando la petición llega por HTTPS y lectura de `X-Forwarded-Proto` detrás de un proxy.
- El login rechaza sin consultar la base de datos nombres y contraseñas desproporcionados.
- El menú se compacta en pantallas medianas para que quepan las nueve secciones en una fila.

## [1.0.0] - 2026-10-23

Primera versión publicada: la práctica rehecha a partir de la entrega.

### Añadido
- Base de datos (MySQL en Docker, H2 en desarrollo) con JDBC y transacciones.
- Alumnos, matrículas con capacidad máxima, exportación a CSV y panel de estadísticas.
- API REST con Jersey sobre los mismos servicios que la web.
- Tests de reglas de negocio e integración con Tomcat embebido.
- Interfaz nueva con modo oscuro, Docker Compose, CI y análisis de seguridad.

### Seguridad
- La API exige sesión, CSRF en todo lo que cambia datos, contraseñas con PBKDF2, bloqueo tras intentos fallidos, escape de toda la salida y CSP sin scripts en línea. El detalle está en el README.

[1.1.0]: https://github.com/alejandrorevillaEng/gestion-universitaria/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/alejandrorevillaEng/gestion-universitaria/releases/tag/v1.0.0
