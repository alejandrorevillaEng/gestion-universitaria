<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="${asignatura.id == 0 ? 'Nueva asignatura' : 'Editar asignatura'}"/>
<c:set var="seccion" value="asignaturas"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1><c:out value="${titulo}"/></h1>
<c:choose>
<c:when test="${empty titulaciones}">
    <p class="vacio">Antes de crear asignaturas necesitas al menos una <a href="${ctx}/titulaciones?accion=nuevo">titulación</a>.</p>
</c:when>
<c:otherwise>
<form method="post" action="${ctx}/asignaturas" class="formulario panel-formulario">
    <input type="hidden" name="_csrf" value="${fn:escapeXml(requestScope.csrfToken)}">
    <c:if test="${asignatura.id != 0}"><input type="hidden" name="id" value="${asignatura.id}"></c:if>

    <label for="nombre">Nombre</label>
    <input id="nombre" name="nombre" maxlength="120" required value="${fn:escapeXml(asignatura.nombre)}">

    <div class="fila">
        <div>
            <label for="creditos">Créditos ECTS</label>
            <input id="creditos" name="creditos" type="number" min="1" max="18" required
                   value="${asignatura.creditos == 0 ? 6 : asignatura.creditos}">
        </div>
        <div>
            <label for="capacidad">Capacidad máxima</label>
            <input id="capacidad" name="capacidad" type="number" min="1" max="500" required
                   value="${asignatura.capacidad == 0 ? '' : asignatura.capacidad}">
        </div>
    </div>

    <label for="idTitulacion">Titulación</label>
    <select id="idTitulacion" name="idTitulacion" required>
        <option value="">Elige una titulación</option>
        <c:forEach var="t" items="${titulaciones}">
            <option value="${t.id}" ${t.id == asignatura.idTitulacion ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
        </c:forEach>
    </select>

    <label for="idProfesor">Profesor</label>
    <select id="idProfesor" name="idProfesor">
        <option value="0">Sin asignar</option>
        <c:forEach var="p" items="${profesores}">
            <option value="${p.id}" ${p.id == asignatura.idProfesor ? 'selected' : ''}><c:out value="${p.nombre}"/></option>
        </c:forEach>
    </select>

    <div class="botones">
        <button type="submit" class="boton">Guardar</button>
        <a class="boton boton-suave" href="${ctx}/asignaturas">Cancelar</a>
    </div>
</form>
</c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
