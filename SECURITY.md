# Política de seguridad

## Cómo informar de una vulnerabilidad

No abras un issue público. Usa el aviso privado de GitHub: pestaña **Security → Report a vulnerability** de este repositorio. Incluye, si puedes, qué parte afecta, cómo reproducirlo y qué impacto crees que tiene.

Es un proyecto personal, así que intentaré responder en unos 7 días.

## Medidas actuales

- Contraseñas con hash PBKDF2-HMAC-SHA256 (600.000 iteraciones) y bloqueo temporal tras varios intentos fallidos.
- Control de acceso centralizado en un filtro que usa la ruta ya normalizada por el contenedor.
- Token CSRF en todos los formularios y en la API; cookie de sesión HttpOnly y SameSite=Lax; id de sesión nuevo al entrar.
- Salida escapada en las vistas (JSTL) y en el JavaScript (`textContent`), con Content-Security-Policy sin scripts en línea.
- Consultas SQL siempre parametrizadas y validación de entrada en la capa de servicio.
- En CI: gitleaks, semgrep y osv-scanner en cada cambio, y Dependabot para mantener las dependencias al día.
