<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Entrar"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="tarjeta-acceso">
    <h1>Gestión universitaria</h1>
    <p class="detalle">Titulaciones, asignaturas, profesores y matrículas.</p>
    <form method="post" action="${ctx}/login" class="formulario">
        <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
        <label for="nombre">Usuario</label>
        <input id="nombre" name="nombre" autocomplete="username" required autofocus value="${fn:escapeXml(nombre)}">
        <label for="contrasena">Contraseña</label>
        <input id="contrasena" name="contrasena" type="password" autocomplete="current-password" required>
        <button type="submit" class="boton boton-ancho">Entrar</button>
    </form>
    <p class="detalle">¿No tienes cuenta? <a href="${ctx}/registro">Regístrate</a></p>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
