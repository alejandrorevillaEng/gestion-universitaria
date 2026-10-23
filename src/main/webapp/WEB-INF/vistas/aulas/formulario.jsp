<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${aula.id == 0 ? 'Nueva aula' : 'Editar aula'}"/>
<c:set var="seccion" value="aulas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<form method="post" action="${ctx}/aulas" class="formulario panel-formulario">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${aula.id != 0}"><input type="hidden" name="id" value="${aula.id}"></c:if>
    <div class="fila">
        <div>
            <label for="nombre">Nombre</label>
            <input id="nombre" name="nombre" maxlength="40" required placeholder="A-101" value="${fn:escapeXml(aula.nombre)}">
        </div>
        <div>
            <label for="capacidad">Plazas</label>
            <input id="capacidad" name="capacidad" type="number" min="1" max="1000" required value="${aula.capacidad == 0 ? '' : aula.capacidad}">
        </div>
    </div>
    <label for="edificio">Edificio <span class="detalle">(opcional)</span></label>
    <input id="edificio" name="edificio" maxlength="80" value="${fn:escapeXml(aula.edificio)}">
    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/aulas">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
