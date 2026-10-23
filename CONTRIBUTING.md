# Cómo contribuir

Gracias por echarle un ojo. Es un proyecto personal, pero acepto issues y pull requests.

## Preparar el entorno

Solo hace falta Java 21. Maven va incluido (`./mvnw`, o `mvnw.cmd` en Windows).

```bash
./mvnw test-compile exec:exec@dev    # app en http://localhost:8080 con H2 y datos de ejemplo
./mvnw verify                        # tests, cobertura mínima y WAR
./mvnw -Pmysql test                  # tests contra MySQL 8.4 (necesita Docker)
```

## Antes de abrir un pull request

- `./mvnw verify` pasa en local. La build falla si la cobertura baja del 75 % de líneas.
- Si cambias una regla de negocio, añade o ajusta su test en `servicio/`.
- Si añades un endpoint, documéntalo en `docs/openapi.yaml`. `ContratoApiTest` falla si no.
- Si cambias el esquema, crea una migración nueva en `src/main/resources/db/migration` (`V3__...sql`). Nunca edites una que ya se haya aplicado.
- Todo lo que venga del usuario se pinta con `<c:out>` o `fn:escapeXml`, y las consultas van con `?`.
- Anota el cambio en `CHANGELOG.md`, en la sección de la próxima versión.

## Commits

Mensajes cortos en español, en imperativo y con un prefijo que diga el tipo: `feat:`, `fix:`, `test:`, `docs:`, `build:` o `refactor:`.

## Seguridad

Las vulnerabilidades no se publican en issues: sigue [SECURITY.md](SECURITY.md).
