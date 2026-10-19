package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.DemandPrediction;

public interface PredictionService {

    /**
     * Método tradicional: promedio móvil sobre el historial de ventas.
     * Si no hay suficiente historial, el resultado trae available=false.
     */
    TraditionalPredictionResult predictTraditional(Long productId, int periods);

    /**
     * Compara el método tradicional contra ML. Si ML falla, devuelve
     * mlAvailable=false y usa igualmente el tradicional (fallback).
     * La llamada real a MachineLearningClient se conecta en la FASE 15.
     */
    ComparisonResult compare(Long productId);

    DemandPrediction saveComparisonResult(Long productId, ComparisonResult result);

    record TraditionalPredictionResult(
            Long productId, int periodsUsed, int dataPointsAnalyzed,
            java.math.BigDecimal estimatedDemand, boolean sufficientData, String method) {}

    record ComparisonResult(
            Long productId,
            java.math.BigDecimal traditionalPrediction,
            boolean traditionalSufficientData,
            java.math.BigDecimal mlPrediction,
            boolean mlAvailable) {}
}