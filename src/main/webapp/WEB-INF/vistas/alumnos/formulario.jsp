<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${alumno.id == 0 ? 'Nuevo alumno' : 'Editar alumno'}"/>
<c:set var="seccion" value="alumnos"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<form method="post" action="${ctx}/alumnos" class="formulario">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${alumno.id != 0}"><input type="hidden" name="id" value="${alumno.id}"></c:if>

    <div class="fila">
        <div>
            <label for="nombre">Nombre</label>
            <input id="nombre" name="nombre" maxlength="80" required value="${fn:escapeXml(alumno.nombre)}">
        </div>
        <div>
            <label for="apellidos">Apellidos</label>
            <input id="apellidos" name="apellidos" maxlength="120" required value="${fn:escapeXml(alumno.apellidos)}">
        </div>
    </div>

    <label for="email">Email</label>
    <input id="email" name="email" type="email" maxlength="160" required value="${fn:escapeXml(alumno.email)}">

    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/alumnos">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
