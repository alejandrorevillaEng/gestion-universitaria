<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Estadísticas"/><c:set var="seccion" value="estadisticas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1>Estadísticas</h1>
<c:set var="e" value="${estadisticas}"/>

<div class="cifras">
    <div class="cifra destacada"><span class="valor">${e.totalAlumnos}</span><span class="nombre">alumnos</span></div>
    <div class="cifra"><span class="valor">${e.totalAsignaturas}</span><span class="nombre">asignaturas</span></div>
    <div class="cifra"><span class="valor">${e.totalMatriculas}</span><span class="nombre">matrículas</span></div>
</div>

<div class="dos-columnas">
<section>
    <h2>Asignaturas por titulación</h2>
    <div class="tabla-movil"><table>
        <thead><tr><th>Titulación</th><th class="num">Asignaturas</th></tr></thead>
        <tbody>
        <c:forEach var="f" items="${e.asignaturasPorTitulacion}">
            <tr><td><c:out value="${f.nombre}"/></td><td class="num">${f.valor}</td></tr>
        </c:forEach>
        </tbody>
    </table></div>
</section>

<section>
    <h2>Ocupación de cada asignatura</h2>
    <div class="tabla-movil"><table>
        <thead><tr><th>Asignatura</th><th>Ocupación</th><th class="num">%</th></tr></thead>
        <tbody>
        <c:forEach var="a" items="${e.ocupacion}">
        <tr>
            <td><a href="${ctx}/asignaturas?accion=ver&amp;id=${a.id}"><c:out value="${a.nombre}"/></a></td>
            <td><meter min="0" max="${a.capacidad}" value="${a.matriculados}" low="${a.capacidad * 0.75}" high="${a.capacidad * 0.9}" optimum="0"
                       title="${a.matriculados} de ${a.capacidad}"></meter></td>
            <td class="num">${a.ocupacion} %</td>
        </tr>
        </c:forEach>
        </tbody>
    </table></div>
</section>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
