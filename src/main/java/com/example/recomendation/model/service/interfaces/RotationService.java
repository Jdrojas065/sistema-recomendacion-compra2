package com.example.recomendation.model.service.interfaces;

public interface RotationService {

    /**
     * Calcula la rotación de un producto (ventas / stock promedio, aproximación simple).
     * Devuelve un objeto con el valor y su clasificación (HIGH/LOW).
     */
    RotationResult calculate(Long productId);

    record RotationResult(Long productId, double rotationValue, String classification) {}
}