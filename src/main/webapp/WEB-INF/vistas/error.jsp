<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="titulo" value="Error ${codigo}"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="tarjeta-acceso">
    <h1>
        <c:choose>
            <c:when test="${codigo == 403}">No tienes permiso para ver esto</c:when>
            <c:when test="${codigo == 404}">No encontramos lo que buscas</c:when>
            <c:when test="${codigo == 400 || codigo == 409}">No se ha podido hacer</c:when>
            <c:otherwise>Algo ha fallado</c:otherwise>
        </c:choose>
    </h1>
    <%-- Solo se muestra el mensaje de los errores controlados (4xx); de un 500 nunca se enseñan detalles. --%>
    <c:if test="${codigo >= 400 && codigo < 500 && not empty requestScope['jakarta.servlet.error.message']}">
        <p><c:out value="${requestScope['jakarta.servlet.error.message']}"/></p>
    </c:if>
    <p><a class="boton" href="${ctx}/asignaturas">Volver al inicio</a></p>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
