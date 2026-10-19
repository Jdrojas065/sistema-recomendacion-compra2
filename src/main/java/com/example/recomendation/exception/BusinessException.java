package com.example.recomendation.exception;

/**
 * Excepción genérica para violaciones de reglas de negocio
 * (ej. stock insuficiente, carrito vacío, nombre duplicado).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}