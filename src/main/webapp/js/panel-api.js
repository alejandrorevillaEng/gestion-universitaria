// Cliente de la API REST para la página "API REST".
// Los datos se pintan siempre con textContent, nunca con innerHTML: un nombre
// con HTML se ve como texto y no se ejecuta.
(() => {
    const base = document.querySelector('link[rel=stylesheet]').getAttribute('href').replace('/css/estilos.css', '') + '/api';
    const token = document.querySelector('meta[name=csrf-token]').content;
    const estado = document.getElementById('estado-api');
    const cache = { titulaciones: [], profesores: [], asignaturas: [] };

    async function api(metodo, ruta, cuerpo) {
        const opciones = { method: metodo, headers: { 'Accept': 'application/json' }, credentials: 'same-origin' };
        if (metodo !== 'GET') opciones.headers['X-CSRF-Token'] = token;
        if (cuerpo !== undefined) {
            opciones.headers['Content-Type'] = 'application/json';
            opciones.body = JSON.stringify(cuerpo);
        }
        const respuesta = await fetch(base + ruta, opciones);
        const texto = await respuesta.text();
        const datos = texto ? JSON.parse(texto) : null;
        if (!respuesta.ok) throw new Error((datos && datos.error) || ('Error ' + respuesta.status));
        return datos;
    }

    function avisar(mensaje, esError) {
        estado.hidden = false;
        estado.textContent = mensaje;
        estado.className = 'aviso ' + (esError ? 'aviso-error' : 'aviso-aviso');
    }

    function celda(fila, texto) {
        const td = document.createElement('td');
        td.textContent = texto == null ? '' : String(texto);
        fila.appendChild(td);
        return td;
    }

    function boton(texto, clase, accion) {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'boton boton-pequeno ' + clase;
        b.textContent = texto;
        b.addEventListener('click', accion);
        return b;
    }

    const columnas = {
        titulaciones: (t) => [t.id, t.nombre, t.descripcion],
        profesores: (p) => [p.id, p.nombre, p.especialidad],
        asignaturas: (a) => [a.id, a.nombre, a.nombreTitulacion, a.nombreProfesor || 'Sin asignar',
            a.matriculados + '/' + a.capacidad],
    };

    function pintar(recurso) {
        const tbody = document.querySelector('#panel-' + recurso + ' tbody');
        tbody.replaceChildren();
        for (const elemento of cache[recurso]) {
            const fila = document.createElement('tr');
            columnas[recurso](elemento).forEach((valor) => celda(fila, valor));
            const acciones = celda(fila, '');
            acciones.className = 'acciones';
            acciones.append(
                boton('Editar', 'boton-suave', () => editar(recurso, elemento)),
                ' ',
                boton('Borrar', 'boton-peligro', () => borrar(recurso, elemento)));
            tbody.appendChild(fila);
        }
    }

    function rellenarDesplegables() {
        document.querySelectorAll('select[data-opciones]').forEach((select) => {
            const valor = select.value;
            select.replaceChildren();
            const vacia = document.createElement('option');
            vacia.value = '';
            vacia.textContent = select.dataset.vacia || 'Elige…';
            select.appendChild(vacia);
            for (const item of cache[select.dataset.opciones]) {
                const opcion = document.createElement('option');
                opcion.value = item.id;
                opcion.textContent = item.nombre;
                select.appendChild(opcion);
            }
            select.value = valor;
        });
    }

    async function cargar() {
        const [t, p, a] = await Promise.all([api('GET', '/titulaciones'), api('GET', '/profesores'), api('GET', '/asignaturas')]);
        cache.titulaciones = t;
        cache.profesores = p;
        cache.asignaturas = a;
        ['titulaciones', 'profesores', 'asignaturas'].forEach(pintar);
        rellenarDesplegables();
    }

    function editar(recurso, elemento) {
        const formulario = document.querySelector('form[data-recurso=' + recurso + ']');
        for (const campo of formulario.elements) {
            if (campo.name && elemento[campo.name] !== undefined) campo.value = elemento[campo.name] ?? '';
        }
        formulario.querySelector('[name=nombre]').focus();
    }

    async function borrar(recurso, elemento) {
        if (!window.confirm('¿Borrar «' + elemento.nombre + '»?')) return;
        try {
            await api('DELETE', '/' + recurso + '/' + elemento.id);
            avisar('Borrado: ' + elemento.nombre);
            await cargar();
        } catch (e) {
            avisar(e.message, true);
        }
    }

    function leerFormulario(formulario) {
        const datos = {};
        for (const campo of formulario.elements) {
            if (!campo.name || campo.name === 'id') continue;
            if (campo.type === 'number' || campo.tagName === 'SELECT') {
                datos[campo.name] = campo.value === '' ? null : Number(campo.value);
            } else {
                datos[campo.name] = campo.value;
            }
        }
        return datos;
    }

    document.querySelectorAll('form[data-recurso]').forEach((formulario) => {
        formulario.addEventListener('submit', async (evento) => {
            evento.preventDefault();
            const recurso = formulario.dataset.recurso;
            const id = formulario.elements.id.value;
            try {
                if (id) await api('PUT', '/' + recurso + '/' + id, leerFormulario(formulario));
                else await api('POST', '/' + recurso, leerFormulario(formulario));
                formulario.reset();
                formulario.elements.id.value = '';
                avisar('Guardado');
                await cargar();
            } catch (e) {
                avisar(e.message, true);
            }
        });
        formulario.addEventListener('reset', () => { formulario.elements.id.value = ''; });
    });

    cargar().catch((e) => avisar(e.message, true));
})();
