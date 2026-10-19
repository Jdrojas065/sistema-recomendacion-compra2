package com.example.recomendation.exception;

/**
 * Se lanza internamente por MachineLearningClient cuando el servicio
 * Python falla (timeout, connection refused, 500, respuesta inválida).
 *
 * IMPORTANTE: esta excepción NO debe llegar nunca hasta el usuario final.
 * Los servicios que la capturan (PredictionService, RecommendationService)
 * deben resolverla internamente usando el método tradicional como fallback
 * y simplemente loguear el fallo — ver sección 13 del prompt.
 */
public class MLServiceException extends RuntimeException {

    public MLServiceException(String message) {
        super(message);
    }

    public MLServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}