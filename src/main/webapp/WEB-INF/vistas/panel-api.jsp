<%@ include file="/WEB-INF/vistas/fragmentos/directivas.jspf" %>
<c:set var="titulo" value="API REST"/><c:set var="seccion" value="api"/>
<%@ include file="/WEB-INF/vistas/fragmentos/cabecera.jspf" %>
<h1>Panel de la API REST</h1>
<p class="detalle">
    Esta página no usa los servlets: todo lo hace JavaScript con <code>fetch</code> contra <code>${ctx}/api</code>.
    Los datos son los mismos que en el resto de la aplicación. La documentación está en
    <code>docs/api.md</code> del repositorio.
</p>

<section class="panel" id="panel-titulaciones">
    <h2>Titulaciones</h2>
    <form class="formulario formulario-linea" data-recurso="titulaciones">
        <input type="hidden" name="id">
        <input name="nombre" placeholder="Nombre" required maxlength="120">
        <input name="descripcion" placeholder="Descripción" maxlength="500">
        <button type="submit" class="boton">Guardar</button>
        <button type="reset" class="boton boton-suave">Limpiar</button>
    </form>
    <div class="tabla-movil"><table><thead><tr><th>Id</th><th>Nombre</th><th>Descripción</th><th></th></tr></thead>
        <tbody></tbody></table></div>
</section>

<section class="panel" id="panel-profesores">
    <h2>Profesores</h2>
    <form class="formulario formulario-linea" data-recurso="profesores">
        <input type="hidden" name="id">
        <input name="nombre" placeholder="Nombre" required maxlength="120">
        <input name="especialidad" placeholder="Especialidad" maxlength="120">
        <button type="submit" class="boton">Guardar</button>
        <button type="reset" class="boton boton-suave">Limpiar</button>
    </form>
    <div class="tabla-movil"><table><thead><tr><th>Id</th><th>Nombre</th><th>Especialidad</th><th></th></tr></thead>
        <tbody></tbody></table></div>
</section>

<section class="panel" id="panel-asignaturas">
    <h2>Asignaturas</h2>
    <form class="formulario formulario-linea" data-recurso="asignaturas">
        <input type="hidden" name="id">
        <input name="nombre" placeholder="Nombre" required maxlength="120">
        <input name="creditos" type="number" min="1" max="18" placeholder="ECTS" required>
        <input name="capacidad" type="number" min="1" max="500" placeholder="Capacidad" required>
        <select name="idTitulacion" required data-opciones="titulaciones"></select>
        <select name="idProfesor" data-opciones="profesores" data-vacia="Sin profesor"></select>
        <button type="submit" class="boton">Guardar</button>
        <button type="reset" class="boton boton-suave">Limpiar</button>
    </form>
    <div class="tabla-movil"><table><thead><tr><th>Id</th><th>Nombre</th><th>Titulación</th><th>Profesor</th><th>Plazas</th><th></th></tr></thead>
        <tbody></tbody></table></div>
</section>

<p class="aviso" id="estado-api" role="status" hidden></p>
<script src="${ctx}/js/panel-api.js" defer></script>
<%@ include file="/WEB-INF/vistas/fragmentos/pie.jspf" %>
