<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Asignaturas"/><c:set var="seccion" value="asignaturas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <div><span class="antetitulo">${pagina.total} en total</span><h1>Asignaturas</h1></div>
    <a class="boton" href="${ctx}/asignaturas?accion=nuevo"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-mas"/></svg>Nueva asignatura</a>
</div>
<c:set var="rutaBuscar" value="/asignaturas"/><c:set var="textoBuscar" value="Buscar por asignatura, titulación o profesor"/>
<%@ include file="/WEB-INF/vistas/fragmentos/buscador.jspf" %>
<c:choose>
<c:when test="${empty asignaturas and not empty param.q}">
    <p class="vacio">Nada coincide con «<c:out value="${param.q}"/>».</p>
</c:when>
<c:when test="${empty asignaturas}">
    <p class="vacio">Todavía no hay asignaturas. Crea primero una titulación y después la asignatura.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead>
    <tr><th>Asignatura</th><th>Titulación</th><th>Profesor</th><th class="num">ECTS</th><th>Ocupación</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach var="a" items="${asignaturas}">
    <tr>
        <td><a href="${ctx}/asignaturas?accion=ver&amp;id=${a.id}"><c:out value="${a.nombre}"/></a></td>
        <td><c:out value="${a.nombreTitulacion}"/></td>
        <td><c:out value="${empty a.nombreProfesor ? 'Sin asignar' : a.nombreProfesor}"/></td>
        <td class="num">${a.creditos}</td>
        <td><span class="ocupacion"><meter min="0" max="${a.capacidad}" value="${a.matriculados}" low="${a.capacidad * 0.75}" high="${a.capacidad * 0.9}" optimum="0"
                   title="${a.matriculados} de ${a.capacidad}"></meter><span class="detalle">${a.matriculados}/${a.capacidad}</span></span></td>
        <td class="acciones">
            <a class="boton boton-suave boton-icono" href="${ctx}/asignaturas?accion=editar&amp;id=${a.id}" title="Editar" aria-label="Editar"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-lapiz"/></svg></a>
            <c:set var="rutaBorrar" value="/asignaturas"/><c:set var="idBorrar" value="${a.id}"/>
            <c:set var="textoConfirmar" value="¿Borrar la asignatura ${a.nombre}?"/>
            <%@ include file="/WEB-INF/vistas/fragmentos/borrar.jspf" %>
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/paginacion.jspf" %>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
