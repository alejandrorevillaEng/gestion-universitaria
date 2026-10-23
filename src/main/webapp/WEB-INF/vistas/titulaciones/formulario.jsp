<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${titulacion.id == 0 ? 'Nueva titulación' : 'Editar titulación'}"/>
<c:set var="seccion" value="titulaciones"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<form method="post" action="${ctx}/titulaciones" class="formulario panel-formulario">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${titulacion.id != 0}"><input type="hidden" name="id" value="${titulacion.id}"></c:if>

    <label for="nombre">Nombre</label>
    <input id="nombre" name="nombre" maxlength="120" required value="${fn:escapeXml(titulacion.nombre)}">

    <label for="descripcion">Descripción <span class="detalle">(opcional)</span></label>
    <textarea id="descripcion" name="descripcion" maxlength="500" rows="3"><c:out value="${titulacion.descripcion}"/></textarea>

    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/titulaciones">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
