// Confirmación antes de enviar los formularios de borrado (data-confirmar="...").
// Va en un fichero aparte porque la CSP no permite JavaScript en línea (onclick).
document.addEventListener('submit', (evento) => {
    const formulario = evento.target;
    const pregunta = formulario.dataset.confirmar;
    if (pregunta && !window.confirm(pregunta)) {
        evento.preventDefault();
    }
});
