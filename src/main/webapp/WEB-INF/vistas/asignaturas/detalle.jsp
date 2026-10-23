<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${asignatura.nombre}"/><c:set var="seccion" value="asignaturas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<p class="migas"><a href="${ctx}/asignaturas">Asignaturas</a> /</p>
<div class="encabezado">
    <h1><c:out value="${asignatura.nombre}"/></h1>
    <a class="boton boton-suave" href="${ctx}/asignaturas?accion=editar&amp;id=${asignatura.id}"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-lapiz"/></svg>Editar</a>
</div>

<dl class="ficha">
    <dt>Titulación</dt>
    <dd><a href="${ctx}/titulaciones?accion=ver&amp;id=${asignatura.idTitulacion}"><c:out value="${asignatura.nombreTitulacion}"/></a></dd>
    <dt>Profesor</dt>
    <dd><c:out value="${empty asignatura.nombreProfesor ? 'Sin asignar' : asignatura.nombreProfesor}"/></dd>
    <dt>Créditos</dt>
    <dd>${asignatura.creditos} ECTS</dd>
    <dt>Ocupación</dt>
    <dd>
        <span class="ocupacion"><meter min="0" max="${asignatura.capacidad}" value="${asignatura.matriculados}" low="${asignatura.capacidad * 0.75}" high="${asignatura.capacidad * 0.9}" optimum="0"></meter>
        ${asignatura.matriculados} de ${asignatura.capacidad} plazas (${asignatura.ocupacion} %)</span>
    </dd>
</dl>

<div class="encabezado">
    <h2>Aulas</h2>
</div>
<c:choose>
<c:when test="${empty aulas}"><p class="vacio">Todavía no tiene aula asignada.</p></c:when>
<c:otherwise>
<div class="chips">
    <c:forEach var="au" items="${aulas}">
    <form method="post" action="${ctx}/asignaturas" class="chip" data-confirmar="${fn:escapeXml('¿Quitar el aula '.concat(au.nombre).concat('?'))}">
        <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
        <input type="hidden" name="accion" value="quitarAula">
        <input type="hidden" name="id" value="${asignatura.id}">
        <input type="hidden" name="aula" value="${au.id}">
        <strong><c:out value="${au.nombre}"/></strong><span class="detalle"><c:out value="${au.edificio}"/> · ${au.capacidad} plazas</span>
        <button type="submit" class="boton boton-peligro boton-icono" title="Quitar aula" aria-label="Quitar aula"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-papelera"/></svg></button>
    </form>
    </c:forEach>
</div>
</c:otherwise>
</c:choose>
<c:if test="${not empty aulasDisponibles}">
<form method="post" action="${ctx}/asignaturas" class="formulario formulario-linea">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <input type="hidden" name="accion" value="asignarAula">
    <input type="hidden" name="id" value="${asignatura.id}">
    <label for="aula">Asignar aula</label>
    <select id="aula" name="aula" required>
        <c:forEach var="au" items="${aulasDisponibles}">
            <option value="${au.id}"><c:out value="${au.nombre}"/> · ${au.capacidad} plazas</option>
        </c:forEach>
    </select>
    <button type="submit" class="boton boton-suave">Asignar</button>
    <span class="detalle">Solo aparecen aulas con al menos ${asignatura.capacidad} plazas.</span>
</form>
</c:if>

<div class="encabezado">
    <h2>Alumnos matriculados</h2>
    <a class="boton boton-suave" href="${ctx}/asignaturas?accion=exportar&amp;id=${asignatura.id}"><svg class="icono" aria-hidden="true"><use href="${ctx}/img/iconos.svg#i-descargar"/></svg>Descargar CSV</a>
</div>

<c:choose>
<c:when test="${asignatura.plazasLibres == 0}">
    <p class="aviso aviso-info">La asignatura está completa: no admite más matrículas.</p>
</c:when>
<c:when test="${empty candidatos}">
    <p class="vacio">No quedan alumnos por matricular. <a href="${ctx}/alumnos?accion=nuevo">Da de alta uno</a>.</p>
</c:when>
<c:otherwise>
<form method="post" action="${ctx}/asignaturas" class="formulario formulario-linea">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <input type="hidden" name="accion" value="matricular">
    <input type="hidden" name="id" value="${asignatura.id}">
    <label for="alumno">Matricular a</label>
    <select id="alumno" name="alumno" required>
        <c:forEach var="al" items="${candidatos}">
            <option value="${al.id}"><c:out value="${al.apellidos}, ${al.nombre}"/></option>
        </c:forEach>
    </select>
    <button type="submit" class="boton">Matricular</button>
    <span class="detalle">${asignatura.plazasLibres} plaza(s) libre(s)</span>
</form>
</c:otherwise>
</c:choose>

<c:choose>
<c:when test="${empty matriculados}">
    <p class="vacio">Nadie matriculado todavía.</p>
</c:when>
<c:otherwise>
<div class="tabla-movil">
<table>
    <thead><tr><th>Apellidos</th><th>Nombre</th><th>Email</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="al" items="${matriculados}">
    <tr>
        <td><a href="${ctx}/alumnos?accion=ver&amp;id=${al.id}"><c:out value="${al.apellidos}"/></a></td>
        <td><c:out value="${al.nombre}"/></td>
        <td><c:out value="${al.email}"/></td>
        <td class="acciones">
            <form method="post" action="${ctx}/asignaturas" class="en-linea"
                  data-confirmar="${fn:escapeXml('¿Desmatricular a '.concat(al.nombre).concat('?'))}">
                <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
                <input type="hidden" name="accion" value="desmatricular">
                <input type="hidden" name="id" value="${asignatura.id}">
                <input type="hidden" name="alumno" value="${al.id}">
                <button type="submit" class="boton boton-suave boton-pequeno">Desmatricular</button>
            </form>
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
