package com.example.recomendation.model.service.impl;

import com.example.recomendation.model.entity.Anomaly;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.SaleDetail;
import com.example.recomendation.model.repository.AnomalyRepository;
import com.example.recomendation.model.repository.SaleDetailRepository;
import com.example.recomendation.model.service.interfaces.AnomalyDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detección de anomalías (RF13) basada en desviación estándar (z-score)
 * sobre el historial de cantidades vendidas del producto. No depende del
 * servicio de Machine Learning: es una comprobación estadística simple que
 * corre en el propio backend cada vez que se confirma una venta.
 *
 * Cómo funciona: por cada línea de venta, compara la cantidad vendida contra
 * el promedio y la desviación estándar de las ventas anteriores de ese mismo
 * producto. Si la cantidad actual está a más de 2 desviaciones estándar por
 * encima del promedio histórico, se considera fuera de lo normal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    // Mínimo de ventas históricas necesarias antes de poder calcular un
    // promedio/desviación confiables. Con menos que esto, no se evalúa.
    private static final int MIN_HISTORY = 5;
    // A partir de cuántas desviaciones estándar por encima del promedio
    // se considera una anomalía.
    private static final double Z_SCORE_THRESHOLD = 2.0;
    // Ventana de historial considerada "patrón habitual" del producto.
    private static final int HISTORY_WINDOW_DAYS = 180;

    private final AnomalyRepository anomalyRepository;
    private final SaleDetailRepository saleDetailRepository;

    @Override
    public void evaluateSale(Product product, int quantitySold) {
        try {
            LocalDateTime start = LocalDateTime.now().minusDays(HISTORY_WINDOW_DAYS);
            List<SaleDetail> history = saleDetailRepository.findByProductIdAndDateRange(
                    product.getId(), start, LocalDateTime.now());

            List<Integer> quantities = history.stream()
                    .map(SaleDetail::getQuantity)
                    .toList();

            if (quantities.size() < MIN_HISTORY + 1) {
                return; // no hay suficiente historial para juzgar qué es "normal" todavía
            }

            // La venta actual ya quedó incluida en "history" (se guarda justo antes
            // de llamar aquí); la excluimos para calcular el promedio SIN ella.
            List<Integer> baseline = quantities.subList(0, quantities.size() - 1);

            double mean = baseline.stream().mapToInt(Integer::intValue).average().orElse(0);
            double variance = baseline.stream()
                    .mapToDouble(q -> Math.pow(q - mean, 2))
                    .average().orElse(0);
            double stdDev = Math.sqrt(variance);

            double zScore;
            if (stdDev > 0) {
                zScore = (quantitySold - mean) / stdDev;
            } else if (mean > 0) {
                // Sin variación histórica (siempre se vendió la misma cantidad):
                // un salto de más del doble también cuenta como anomalía.
                zScore = quantitySold > mean * 2 ? Z_SCORE_THRESHOLD : 0;
            } else {
                zScore = 0;
            }

            if (zScore < Z_SCORE_THRESHOLD) {
                return; // dentro de lo esperado, no es anomalía
            }

            String severity = zScore >= 3.0 ? "HIGH" : "MEDIUM";

            Anomaly anomaly = Anomaly.builder()
                    .product(product)
                    .date(LocalDateTime.now())
                    .expectedValue(BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP))
                    .observedValue(BigDecimal.valueOf(quantitySold))
                    .deviation(BigDecimal.valueOf(zScore).setScale(2, RoundingMode.HALF_UP))
                    .severity(severity)
                    .description("Venta de " + quantitySold + " unidades, muy por encima del promedio habitual de "
                            + BigDecimal.valueOf(mean).setScale(1, RoundingMode.HALF_UP) + " unidades por venta.")
                    .build();

            anomalyRepository.save(anomaly);
            log.info("Anomalía detectada para producto {}: {} unidades vendidas (z-score {})",
                    product.getId(), quantitySold, String.format("%.2f", zScore));

        } catch (Exception ex) {
            // La detección de anomalías es una mejora adicional; nunca debe
            // hacer fallar el registro de la venta en sí.
            log.warn("No se pudo evaluar anomalías para el producto {}: {}", product.getId(), ex.getMessage());
        }
    }
}