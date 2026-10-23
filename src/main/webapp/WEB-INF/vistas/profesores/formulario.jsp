<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${profesor.id == 0 ? 'Nuevo profesor' : 'Editar profesor'}"/>
<c:set var="seccion" value="profesores"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<form method="post" action="${ctx}/profesores" class="formulario panel-formulario">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${profesor.id != 0}"><input type="hidden" name="id" value="${profesor.id}"></c:if>

    <label for="nombre">Nombre</label>
    <input id="nombre" name="nombre" maxlength="120" required value="${fn:escapeXml(profesor.nombre)}">

    <label for="especialidad">Especialidad <span class="detalle">(opcional)</span></label>
    <input id="especialidad" name="especialidad" maxlength="120" value="${fn:escapeXml(profesor.especialidad)}">

    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/profesores">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
