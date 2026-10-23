<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Titulaciones"/><c:set var="seccion" value="titulaciones"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <h1>Titulaciones</h1>
    <a class="boton" href="${ctx}/titulaciones?accion=nuevo">Nueva titulación</a>
</div>
<c:choose>
<c:when test="${empty titulaciones}">
    <p class="vacio">No hay titulaciones todavía.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Titulación</th><th>Descripción</th><th class="num">Asignaturas</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="t" items="${titulaciones}">
    <tr>
        <td><a href="${ctx}/titulaciones?accion=ver&amp;id=${t.id}"><c:out value="${t.nombre}"/></a></td>
        <td class="detalle"><c:out value="${t.descripcion}"/></td>
        <td class="num">${t.numAsignaturas}</td>
        <td class="acciones">
            <a class="boton boton-suave boton-pequeno" href="${ctx}/titulaciones?accion=editar&amp;id=${t.id}">Editar</a>
            <c:set var="rutaBorrar" value="/titulaciones"/><c:set var="idBorrar" value="${t.id}"/>
            <c:set var="textoConfirmar" value="¿Borrar la titulación ${t.nombre}?"/>
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
