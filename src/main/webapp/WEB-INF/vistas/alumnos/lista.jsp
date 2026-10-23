<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Alumnos"/><c:set var="seccion" value="alumnos"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <h1>Alumnos</h1>
    <a class="boton" href="${ctx}/alumnos?accion=nuevo">Nuevo alumno</a>
</div>
<c:choose>
<c:when test="${empty alumnos}">
    <p class="vacio">No hay alumnos todavía.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Apellidos</th><th>Nombre</th><th>Email</th><th class="num">Asignaturas</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="al" items="${alumnos}">
    <tr>
        <td><a href="${ctx}/alumnos?accion=ver&amp;id=${al.id}"><c:out value="${al.apellidos}"/></a></td>
        <td><c:out value="${al.nombre}"/></td>
        <td class="detalle"><c:out value="${al.email}"/></td>
        <td class="num">${al.numAsignaturas}</td>
        <td class="acciones">
            <a class="boton boton-suave boton-pequeno" href="${ctx}/alumnos?accion=editar&amp;id=${al.id}">Editar</a>
            <c:set var="rutaBorrar" value="/alumnos"/><c:set var="idBorrar" value="${al.id}"/>
            <c:set var="textoConfirmar" value="¿Borrar a ${al.nombre} ${al.apellidos} y sus matrículas?"/>
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
