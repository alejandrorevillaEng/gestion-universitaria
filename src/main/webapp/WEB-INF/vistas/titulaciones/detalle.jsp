<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${titulacion.nombre}"/><c:set var="seccion" value="titulaciones"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<p class="migas"><a href="${ctx}/titulaciones">Titulaciones</a> /</p>
<div class="encabezado">
    <h1><c:out value="${titulacion.nombre}"/></h1>
    <a class="boton" href="${ctx}/asignaturas?accion=nuevo&amp;titulacion=${titulacion.id}">Añadir asignatura</a>
</div>
<c:if test="${not empty titulacion.descripcion}"><p class="detalle"><c:out value="${titulacion.descripcion}"/></p></c:if>

<c:choose>
<c:when test="${empty asignaturas}">
    <p class="vacio">Esta titulación aún no tiene asignaturas.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Asignatura</th><th>Profesor</th><th class="num">ECTS</th><th>Ocupación</th></tr></thead>
    <tbody>
    <c:forEach var="a" items="${asignaturas}">
    <tr>
        <td><a href="${ctx}/asignaturas?accion=ver&amp;id=${a.id}"><c:out value="${a.nombre}"/></a></td>
        <td><c:out value="${empty a.nombreProfesor ? 'Sin asignar' : a.nombreProfesor}"/></td>
        <td class="num">${a.creditos}</td>
        <td><span class="ocupacion"><meter min="0" max="${a.capacidad}" value="${a.matriculados}" low="${a.capacidad * 0.75}" high="${a.capacidad * 0.9}" optimum="0"></meter><span class="detalle">${a.matriculados}/${a.capacidad}</span></span></td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
