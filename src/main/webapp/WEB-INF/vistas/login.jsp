<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Entrar"/><c:set var="pantallaAcceso" value="true"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<%@ include file="/WEB-INF/vistas/fragmentos/panel-marca.jspf" %>
<section class="acceso-formulario">
    <div class="tarjeta-acceso">
        <h1>Hola de nuevo</h1>
        <p class="detalle">Entra con tu usuario para gestionar la universidad.</p>
        <c:if test="${not empty sessionScope.flash}">
            <div class="aviso aviso-${sessionScope.flash.tipo}" role="status"><c:out value="${sessionScope.flash.mensaje}"/></div>
            <c:remove var="flash" scope="session"/>
        </c:if>
        <c:if test="${not empty errorFormulario}">
            <div class="aviso aviso-error" role="alert"><c:out value="${errorFormulario}"/></div>
        </c:if>
        <form method="post" action="${ctx}/login" class="formulario">
            <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
            <label for="nombre">Usuario</label>
            <input id="nombre" name="nombre" autocomplete="username" required autofocus value="${fn:escapeXml(nombre)}">
            <label for="contrasena">Contraseña</label>
            <input id="contrasena" name="contrasena" type="password" autocomplete="current-password" required>
            <button type="submit" class="boton boton-ancho">Entrar</button>
        </form>
        <p class="detalle">¿No tienes cuenta? <a href="${ctx}/registro">Regístrate</a></p>
        <p class="seguro"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-candado"/></svg>Contraseñas con PBKDF2 y bloqueo tras varios intentos fallidos</p>
    </div>
</section>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
