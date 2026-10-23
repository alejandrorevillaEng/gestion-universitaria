<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="Usuarios"/><c:set var="seccion" value="usuarios"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<div class="encabezado">
    <h1>Usuarios</h1>
    <a class="boton" href="${ctx}/usuarios?accion=nuevo">Nuevo usuario</a>
</div>
<div class="tabla-movil">
<table>
    <thead><tr><th>Usuario</th><th>Rol</th><th></th></tr></thead>
    <tbody>
    <c:forEach var="u" items="${usuarios}">
    <tr>
        <td><c:out value="${u.nombre}"/><c:if test="${u.id == yo.id}"> <span class="etiqueta">tú</span></c:if></td>
        <td>${u.rol == 'ADMIN' ? 'Administrador' : 'Usuario'}</td>
        <td class="acciones">
            <a class="boton boton-suave boton-pequeno" href="${ctx}/usuarios?accion=editar&amp;id=${u.id}">Editar</a>
            <c:if test="${u.id != yo.id}">
                <c:set var="rutaBorrar" value="/usuarios"/><c:set var="idBorrar" value="${u.id}"/>
                <c:set var="textoConfirmar" value="¿Borrar el usuario ${u.nombre}?"/>
                <%@ include file="/WEB-INF/vistas/fragmentos/borrar.jspf" %>
            </c:if>
        </td>
    </tr>
    </c:forEach>
    </tbody>
</table>
</div>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
