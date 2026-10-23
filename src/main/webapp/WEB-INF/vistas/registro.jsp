<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Crear cuenta"/><c:set var="pantallaAcceso" value="true"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<%@ include file="/WEB-INF/vistas/fragmentos/panel-marca.jspf" %>
<section class="acceso-formulario">
    <div class="tarjeta-acceso">
        <h1>Crear cuenta</h1>
        <p class="detalle">Las cuentas nuevas entran como usuario normal; la gestión de usuarios es solo para administración.</p>
        <c:if test="${not empty errorFormulario}">
            <div class="aviso aviso-error" role="alert"><c:out value="${errorFormulario}"/></div>
        </c:if>
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
</section>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
