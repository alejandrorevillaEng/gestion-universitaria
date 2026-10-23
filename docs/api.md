# API REST

Todas las rutas cuelgan de `/api`, devuelven JSON y comparten datos y reglas con la web (los servlets y los recursos REST llaman a los mismos servicios).

La especificación completa está en [`openapi.yaml`](openapi.yaml) (OpenAPI 3.1). Se puede abrir en [Swagger Editor](https://editor.swagger.io) o importar en Postman. Un test compara sus rutas con las del código, así que no se queda desactualizada.

## Autenticación

La API usa la misma sesión que la web. Desde fuera (curl, Postman) primero se inicia sesión:

```bash
curl -c cookies.txt -H "Content-Type: application/json" \
     -d '{"usuario":"admin","contrasena":"admin"}' \
     http://localhost:8080/api/sesion
# {"csrfToken":"…","usuario":{"id":1,"nombre":"admin","rol":"ADMIN"}}
```

Las peticiones que cambian datos (POST, PUT, DELETE) tienen que llevar el token en la cabecera `X-CSRF-Token`:

```bash
curl -b cookies.txt -H "X-CSRF-Token: <token>" -H "Content-Type: application/json" \
     -d '{"nombre":"Grado en Ciencia de Datos"}' \
     http://localhost:8080/api/titulaciones
```

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/sesion` | Inicia sesión y devuelve el token CSRF |
| GET | `/sesion` | Usuario actual y token |
| DELETE | `/sesion` | Cierra la sesión |

## Recursos

| Método | Ruta | Qué hace | Respuesta |
|---|---|---|---|
| GET | `/titulaciones` | Lista | 200 |
| GET | `/titulaciones/{id}` | Una titulación | 200, 404 |
| GET | `/titulaciones/{id}/asignaturas` | Sus asignaturas | 200, 404 |
| POST | `/titulaciones` | Crea `{nombre, descripcion}` | 201 + `Location` |
| PUT | `/titulaciones/{id}` | Modifica | 200, 404 |
| DELETE | `/titulaciones/{id}` | Borra; 409 si tiene asignaturas | 204, 409 |
| GET, POST | `/profesores` | Lista y alta `{nombre, especialidad}` | 200, 201 |
| GET, PUT, DELETE | `/profesores/{id}` | Uno; no se borra si imparte asignaturas | 200, 204, 404, 409 |
| GET, POST | `/asignaturas` | Lista (o busca, ver abajo) y alta `{nombre, creditos, capacidad, idTitulacion, idProfesor}` | 200, 201 |
| GET, PUT, DELETE | `/asignaturas/{id}` | Una; no se borra con alumnos matriculados | 200, 204, 404, 409 |
| PUT | `/asignaturas/{id}/profesor/{idProfesor}` | Asigna profesor | 200, 404 |
| DELETE | `/asignaturas/{id}/profesor` | La deja sin profesor | 200 |
| GET | `/asignaturas/{id}/alumnos` | Matriculados | 200 |
| GET | `/asignaturas/{id}/alumnos.csv` | Matriculados en CSV | 200 `text/csv` |
| PUT | `/asignaturas/{id}/alumnos/{idAlumno}` | Matricula; 409 si está completa o ya matriculado | 204, 409 |
| DELETE | `/asignaturas/{id}/alumnos/{idAlumno}` | Desmatricula | 204, 404 |
| GET | `/asignaturas/{id}/aulas` | Aulas asignadas | 200, 404 |
| PUT | `/asignaturas/{id}/aulas/{idAula}` | Asigna aula; 409 si no caben los alumnos de la asignatura | 204, 409 |
| DELETE | `/asignaturas/{id}/aulas/{idAula}` | Quita el aula | 204, 404 |
| GET, POST | `/alumnos` | Lista y alta `{nombre, apellidos, email}` | 200, 201 |
| GET, PUT, DELETE | `/alumnos/{id}` | Uno; al borrarlo se borran sus matrículas | 200, 204, 404 |
| GET | `/alumnos/{id}/asignaturas` | Asignaturas del alumno | 200 |
| GET, POST | `/aulas` | Lista y alta `{nombre, edificio, capacidad}` | 200, 201 |
| GET, PUT, DELETE | `/aulas/{id}` | Una; no se borra si tiene asignaturas ni se le baja la capacidad por debajo de una asignada | 200, 204, 404, 409 |
| GET, POST | `/usuarios` | Solo administradores. Alta `{nombre, contrasena, rol}` | 200, 201, 403 |
| GET, PUT, DELETE | `/usuarios/{id}` | Solo administradores | 200, 204, 403, 409 |
| GET | `/estadisticas` | Solo administradores | 200, 403 |
| GET | `/salud` | Pública. `{"estado":"ok"}` si la app llega a la base de datos; 503 si no. La usa el `HEALTHCHECK` de Docker | 200, 503 |

## Búsqueda y paginación

`GET /asignaturas` y `GET /alumnos` sin parámetros devuelven la lista entera, como siempre. Con cualquiera de estos parámetros filtran y paginan, y el total va en la cabecera `X-Total-Count`:

| Parámetro | Qué es | Por defecto |
|---|---|---|
| `q` | Texto a buscar, sin distinguir mayúsculas. `%` y `_` se buscan tal cual | sin filtro |
| `pagina` | Página, desde 1 | 1 |
| `tam` | Elementos por página, hasta 100 | 20 |

```bash
curl -i -b cookies.txt "http://localhost:8080/api/alumnos?q=gil&tam=10"
# X-Total-Count: 1
```

## Errores

Siempre con el mismo formato:

```json
{"error": "La asignatura está completa (30 plazas)"}
```

| Código | Cuándo |
|---|---|
| 400 | Datos que no pasan la validación o JSON mal formado |
| 401 | Sin sesión, o credenciales incorrectas en `/sesion` |
| 403 | Falta el token CSRF, no eres administrador o el login está bloqueado por intentos fallidos |
| 404 | No existe |
| 409 | Choca con una regla: capacidad completa, borrado con dependencias, nombre repetido… |
| 500 | Error interno (se registra en el servidor; la respuesta no da detalles) |
