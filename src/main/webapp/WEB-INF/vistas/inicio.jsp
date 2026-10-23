<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="titulo" value="Inicio"/><c:set var="seccion" value="inicio"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<c:set var="e" value="${estadisticas}"/>
<div class="saludo">
    <span class="antetitulo"><u:fecha/></span>
    <h1>Hola, <c:out value="${yo.nombre}"/></h1>
    <p>Así está la universidad ahora mismo.</p>
</div>

<div class="cifras">
    <a class="cifra destacada" href="${ctx}/alumnos">
        <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-alumnos"/></svg>
        <span class="valor">${e.totalAlumnos}</span><span class="nombre">alumnos</span>
    </a>
    <a class="cifra" href="${ctx}/asignaturas">
        <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-asignaturas"/></svg>
        <span class="valor">${e.totalAsignaturas}</span><span class="nombre">asignaturas</span>
    </a>
    <a class="cifra" href="${ctx}/titulaciones">
        <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-titulaciones"/></svg>
        <span class="valor">${totalTitulaciones}</span><span class="nombre">titulaciones</span>
    </a>
    <div class="cifra">
        <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-estadisticas"/></svg>
        <span class="valor"><fmt:formatNumber value="${ocupacionMedia}" maxFractionDigits="0"/> %</span>
        <span class="nombre">ocupación media · ${e.totalMatriculas} matrículas</span>
    </div>
</div>

<div class="dos-columnas">
    <section>
        <div class="encabezado">
            <h2>Asignaturas que más se llenan</h2>
            <a class="enlace-accion" href="${ctx}/asignaturas">Ver todas <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-flecha"/></svg></a>
        </div>
        <c:choose>
        <c:when test="${empty casiLlenas}">
            <p class="vacio">Aún no hay asignaturas. Empieza creando una titulación.</p>
        </c:when>
        <c:otherwise>
        <div class="tabla-movil">
        <table>
            <thead><tr><th>Asignatura</th><th>Profesor</th><th>Ocupación</th></tr></thead>
            <tbody>
            <c:forEach var="a" items="${casiLlenas}">
            <tr>
                <td><a href="${ctx}/asignaturas?accion=ver&amp;id=${a.id}"><c:out value="${a.nombre}"/></a>
                    <div class="detalle"><c:out value="${a.nombreTitulacion}"/></div></td>
                <td><c:out value="${empty a.nombreProfesor ? 'Sin asignar' : a.nombreProfesor}"/></td>
                <td><span class="ocupacion"><meter min="0" max="${a.capacidad}" value="${a.matriculados}" low="${a.capacidad * 0.75}" high="${a.capacidad * 0.9}" optimum="0"></meter>
                    <span class="detalle">${a.matriculados}/${a.capacidad}</span></span></td>
            </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
        </c:otherwise>
        </c:choose>
    </section>

    <section>
        <div class="encabezado"><h2>Accesos rápidos</h2></div>
        <div class="atajos">
            <a class="atajo" href="${ctx}/asignaturas?accion=nuevo">
                <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-mas"/></svg>
                <span>Nueva asignatura<small>Con su titulación, profesor y plazas</small></span>
                <svg class="icono flecha" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-flecha"/></svg></a>
            <a class="atajo" href="${ctx}/alumnos?accion=nuevo">
                <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-alumnos"/></svg>
                <span>Dar de alta un alumno<small>Después se matricula desde cada asignatura</small></span>
                <svg class="icono flecha" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-flecha"/></svg></a>
            <a class="atajo" href="${ctx}/panel-api">
                <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-api"/></svg>
                <span>Probar la API REST<small>Los mismos datos, desde JavaScript</small></span>
                <svg class="icono flecha" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-flecha"/></svg></a>
            <c:if test="${yo.esAdmin()}">
            <a class="atajo" href="${ctx}/estadisticas">
                <svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-estadisticas"/></svg>
                <span>Estadísticas completas<small>Ocupación de todas las asignaturas</small></span>
                <svg class="icono flecha" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-flecha"/></svg></a>
            </c:if>
        </div>
    </section>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
