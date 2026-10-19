package com.example.recomendation.exception;

/**
 * Se lanza cuando se intenta guardar un producto con un código de barras
 * que ya está en uso por otro producto activo.
 */
public class DuplicateBarcodeException extends BusinessException {
    public DuplicateBarcodeException(String message) {
        super(message);
    }
}