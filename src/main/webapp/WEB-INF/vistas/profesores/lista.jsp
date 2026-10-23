<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Profesores"/><c:set var="seccion" value="profesores"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <h1>Profesores</h1>
    <a class="boton" href="${ctx}/profesores?accion=nuevo">Nuevo profesor</a>
</div>
<c:choose>
<c:when test="${empty profesores}">
    <p class="vacio">No hay profesores todavía.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Nombre</th><th>Especialidad</th><th class="num">Asignaturas</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="p" items="${profesores}">
    <tr>
        <td><c:out value="${p.nombre}"/></td>
        <td class="detalle"><c:out value="${p.especialidad}"/></td>
        <td class="num">${p.numAsignaturas}</td>
        <td class="acciones">
            <a class="boton boton-suave boton-pequeno" href="${ctx}/profesores?accion=editar&amp;id=${p.id}">Editar</a>
            <c:set var="rutaBorrar" value="/profesores"/><c:set var="idBorrar" value="${p.id}"/>
            <c:set var="textoConfirmar" value="¿Borrar a ${p.nombre}?"/>
            <%@ include file="/WEB-INF/vistas/fragmentos/borrar.jspf" %>
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
