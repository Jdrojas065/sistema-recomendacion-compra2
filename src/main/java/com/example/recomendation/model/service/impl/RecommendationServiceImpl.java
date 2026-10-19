package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.PurchaseRecommendation;
import com.example.recomendation.model.enums.RiskLevel;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.repository.PurchaseRecommendationRepository;
import com.example.recomendation.model.service.interfaces.PredictionService;
import com.example.recomendation.model.service.interfaces.RecommendationService;
import com.example.recomendation.model.service.interfaces.RotationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;
    private final PurchaseRecommendationRepository recommendationRepository;
    private final PredictionService predictionService;
    private final RotationService rotationService;
    private final com.example.recomendation.model.repository.AnomalyRepository anomalyRepository;

    // Si el producto tuvo alguna anomalía dentro de esta ventana, su prioridad sube un nivel.
    private static final int RECENT_ANOMALY_WINDOW_DAYS = 14;

    @Override
    public Page<PurchaseRecommendation> findAll(Pageable pageable) {
        return recommendationRepository.findAllByOrderByPriorityAscRecommendationDateDesc(pageable);
    }

    @Override
    public PurchaseRecommendation findByProduct(Long productId) {
        return recommendationRepository.findByProductIdOrderByIdDesc(productId).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay recomendación registrada para el producto con id: " + productId));
    }

    @Override
    public RiskLevel calculateStockoutRisk(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        PredictionService.TraditionalPredictionResult prediction =
                predictionService.predictTraditional(productId, 3);

        if (!prediction.sufficientData()) {
            return RiskLevel.MEDIUM; // sin datos suficientes, se asume un riesgo intermedio por precaución
        }

        double predictedDemand = prediction.estimatedDemand().doubleValue();
        double currentStock = product.getCurrentStock();

        if (currentStock <= 0 || currentStock < predictedDemand * 0.5) {
            return RiskLevel.HIGH;
        }
        if (currentStock < predictedDemand) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
    
    @Override
    public List<PurchaseRecommendation> findTop5ForDashboard() {
        return recommendationRepository.findTop5ByOrderByPriorityAscRecommendationDateDesc();
    }

    @Override
    @Transactional
    public PurchaseRecommendation generateRecommendation(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        PredictionService.TraditionalPredictionResult prediction =
                predictionService.predictTraditional(productId, 3);
        RotationService.RotationResult rotation = rotationService.calculate(productId);
        RiskLevel stockoutRisk = calculateStockoutRisk(productId);

        BigDecimal predictedDemand = prediction.sufficientData() ? prediction.estimatedDemand() : BigDecimal.ZERO;

        int recommendedQuantity = Math.max(
                0, predictedDemand.intValue() + product.getMinimumStock() - product.getCurrentStock());

        boolean hasRecentAnomaly = anomalyRepository.existsByProductIdAndDateAfter(
                productId, java.time.LocalDateTime.now().minusDays(RECENT_ANOMALY_WINDOW_DAYS));

        // RF13: si hubo una anomalía reciente, la prioridad sube un nivel
        // (LOW -> MEDIUM -> HIGH; si ya era HIGH, se queda en HIGH).
        RiskLevel priority = hasRecentAnomaly ? escalate(stockoutRisk) : stockoutRisk;

        String reason = buildReason(stockoutRisk, rotation.classification(), recommendedQuantity);
        if (hasRecentAnomaly) {
            reason += " Se detectó una venta con un comportamiento fuera de lo habitual en los últimos "
                    + RECENT_ANOMALY_WINDOW_DAYS + " días, por lo que se priorizó la revisión de este producto.";
        }

        // Upsert: si el producto ya tenía una recomendación, se actualiza en vez de
        // crear una fila nueva cada vez (evita duplicados acumulándose en el listado).
        PurchaseRecommendation recommendation = recommendationRepository
                .findByProductIdOrderByIdDesc(productId).stream()
                .findFirst()
                .orElseGet(PurchaseRecommendation::new);

        recommendation.setProduct(product);
        recommendation.setRecommendationDate(LocalDate.now());
        recommendation.setPredictedDemand(predictedDemand);
        recommendation.setCurrentStock(product.getCurrentStock());
        recommendation.setRotation(rotation.classification());
        recommendation.setStockoutRisk(stockoutRisk);
        recommendation.setRecommendedQuantity(recommendedQuantity);
        recommendation.setPriority(priority);
        recommendation.setReason(reason);
        recommendation.setSource(prediction.sufficientData() ? "TRADITIONAL" : "INSUFFICIENT_DATA");
        recommendation.setStatus("PENDING");

        return recommendationRepository.save(recommendation);
    }

    private String buildReason(RiskLevel risk, String rotation, int recommendedQuantity) {
        if (recommendedQuantity == 0) {
            return "El stock actual cubre la demanda proyectada; no se requiere reabastecimiento inmediato.";
        }
        return switch (risk) {
            case HIGH -> "La demanda proyectada supera significativamente el stock disponible (rotación " + rotation + ").";
            case MEDIUM -> "El stock disponible está cerca del límite frente a la demanda proyectada (rotación " + rotation + ").";
            case LOW -> "Reabastecimiento preventivo recomendado según la rotación del producto (" + rotation + ").";
        };
    }
    private RiskLevel escalate(RiskLevel risk) {
        return switch (risk) {
            case LOW -> RiskLevel.MEDIUM;
            case MEDIUM -> RiskLevel.HIGH;
            case HIGH -> RiskLevel.HIGH;
        };
    }
}