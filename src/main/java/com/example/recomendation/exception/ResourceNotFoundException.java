package com.example.recomendation.exception;

/**
 * Se lanza cuando se busca una entidad por id (u otro criterio único)
 * y no existe. Se traduce a una página 404 propia.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}