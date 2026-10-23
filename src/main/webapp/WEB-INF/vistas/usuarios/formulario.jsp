<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${usuarioEditado.id == 0 ? 'Nuevo usuario' : 'Editar usuario'}"/>
<c:set var="seccion" value="usuarios"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<form method="post" action="${ctx}/usuarios" class="formulario panel-formulario" autocomplete="off">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${usuarioEditado.id != 0}"><input type="hidden" name="id" value="${usuarioEditado.id}"></c:if>

    <label for="nombre">Usuario</label>
    <input id="nombre" name="nombre" maxlength="50" required pattern="[\p{L}\p{N}._\-]+"
           value="${fn:escapeXml(usuarioEditado.nombre)}">

    <label for="contrasena">Contraseña
        <c:if test="${usuarioEditado.id != 0}"><span class="detalle">(déjala vacía para no cambiarla)</span></c:if>
    </label>
    <input id="contrasena" name="contrasena" type="password" minlength="8" maxlength="128"
           autocomplete="new-password" ${usuarioEditado.id == 0 ? 'required' : ''}>

    <label for="rol">Rol</label>
    <select id="rol" name="rol">
        <option value="USUARIO" ${usuarioEditado.rol == 'USUARIO' ? 'selected' : ''}>Usuario</option>
        <option value="ADMIN" ${usuarioEditado.rol == 'ADMIN' ? 'selected' : ''}>Administrador</option>
    </select>

    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/usuarios">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
