package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.Product;

import java.time.LocalDate;
import java.util.List;

public interface StockProjectionService {

    /**
     * Detecta productos que TODAVÍA no están en stock bajo, pero cuyo ritmo
     * reciente de ventas proyecta que van a cruzar el mínimo dentro del
     * horizonte de alerta (ver WARNING_HORIZON_DAYS en la implementación).
     * A diferencia de la alerta reactiva del dashboard (stock <= mínimo, ya ocurrió),
     * esta es preventiva: avisa ANTES de que pase.
     */
    List<StockProjection> findEarlyWarnings();

    record StockProjection(
            Product product,
            double recentDailyAvg,
            double baselineDailyAvg,
            boolean trendAccelerating,
            long daysUntilMinimum,
            long daysUntilStockout,
            LocalDate projectedMinimumDate,
            LocalDate projectedStockoutDate) {}
}