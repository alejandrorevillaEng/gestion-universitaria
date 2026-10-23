<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Crear cuenta"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="tarjeta-acceso">
    <h1>Crear cuenta</h1>
    <form method="post" action="${ctx}/registro" class="formulario">
        <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
        <label for="nombre">Usuario</label>
        <input id="nombre" name="nombre" maxlength="50" autocomplete="username" required
               pattern="[\p{L}\p{N}._\-]+" title="Letras, números, punto, guion y guion bajo"
               value="${fn:escapeXml(nombre)}">
        <label for="contrasena">Contraseña <span class="detalle">(mínimo 8 caracteres)</span></label>
        <input id="contrasena" name="contrasena" type="password" minlength="8" maxlength="128"
               autocomplete="new-password" required>
        <label for="repetir">Repite la contraseña</label>
        <input id="repetir" name="repetir" type="password" minlength="8" maxlength="128"
               autocomplete="new-password" required>
        <button type="submit" class="boton boton-ancho">Crear cuenta</button>
    </form>
    <p class="detalle">¿Ya tienes cuenta? <a href="${ctx}/login">Entra</a></p>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
