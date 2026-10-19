// Foco automático en el campo de código de barras para que un lector USB
// (que funciona como teclado) pueda escribir directamente sin que el
// cajero tenga que hacer clic en el campo cada vez.
document.addEventListener('DOMContentLoaded', function () {
    var barcodeInput = document.getElementById('barcodeInput');
    var barcodeForm = document.getElementById('barcodeForm');

    if (!barcodeInput || !barcodeForm) {
        return;
    }

    barcodeInput.focus();

    // La mayoría de lectores de código de barras envían un "Enter" al terminar de leer.
    barcodeInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            if (barcodeInput.value.trim().length > 0) {
                barcodeForm.submit();
            }
        }
    });

    // Tras recargar la página (tras agregar o si hubo error), vuelve a enfocar el campo.
    window.addEventListener('load', function () {
        barcodeInput.value = '';
        barcodeInput.focus();
    });
});