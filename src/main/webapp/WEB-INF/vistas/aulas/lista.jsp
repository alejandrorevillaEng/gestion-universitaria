<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Aulas"/><c:set var="seccion" value="aulas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <div><span class="antetitulo">${fn:length(aulas)} en total</span><h1>Aulas</h1></div>
    <a class="boton" href="${ctx}/aulas?accion=nuevo"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-mas"/></svg>Nueva aula</a>
</div>
<c:choose>
<c:when test="${empty aulas}">
    <p class="vacio">No hay aulas todavía.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Aula</th><th>Edificio</th><th class="num">Plazas</th><th class="num">Asignaturas</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="a" items="${aulas}">
    <tr>
        <td><strong><c:out value="${a.nombre}"/></strong></td>
        <td class="detalle"><c:out value="${a.edificio}"/></td>
        <td class="num">${a.capacidad}</td>
        <td class="num">${a.numAsignaturas}</td>
        <td class="acciones">
            <a class="boton boton-suave boton-icono" href="${ctx}/aulas?accion=editar&amp;id=${a.id}" title="Editar" aria-label="Editar"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-lapiz"/></svg></a>
            <c:set var="rutaBorrar" value="/aulas"/><c:set var="idBorrar" value="${a.id}"/>
            <c:set var="textoConfirmar" value="¿Borrar el aula ${a.nombre}?"/>
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
