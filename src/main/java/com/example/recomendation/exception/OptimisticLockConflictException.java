package com.example.recomendation.exception;

/**
 * Se lanza cuando ObjectOptimisticLockingFailureException ocurre al
 * actualizar un Product cuyo @Version cambió por una operación concurrente
 * (otra venta o movimiento de inventario procesado al mismo tiempo).
 */
public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }
}