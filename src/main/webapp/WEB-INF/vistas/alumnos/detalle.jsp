<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${alumno.nombre} ${alumno.apellidos}"/><c:set var="seccion" value="alumnos"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<p class="migas"><a href="${ctx}/alumnos">Alumnos</a> /</p>
<div class="encabezado">
    <h1><c:out value="${alumno.nombre} ${alumno.apellidos}"/></h1>
    <a class="boton boton-suave" href="${ctx}/alumnos?accion=editar&amp;id=${alumno.id}">Editar</a>
</div>
<p class="detalle"><c:out value="${alumno.email}"/></p>

<h2>Asignaturas en las que está matriculado</h2>
<c:choose>
<c:when test="${empty asignaturas}">
    <p class="vacio">No está matriculado en ninguna. Se matricula desde la ficha de cada asignatura.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Asignatura</th><th>Titulación</th><th class="num">ECTS</th></tr></thead>
    <tbody>
    <c:forEach var="a" items="${asignaturas}">
    <tr>
        <td><a href="${ctx}/asignaturas?accion=ver&amp;id=${a.id}"><c:out value="${a.nombre}"/></a></td>
        <td><c:out value="${a.nombreTitulacion}"/></td>
        <td class="num">${a.creditos}</td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
