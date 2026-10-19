package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.PurchaseRecommendation;
import com.example.recomendation.model.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RecommendationService {

    Page<PurchaseRecommendation> findAll(Pageable pageable);
    List<PurchaseRecommendation> findTop5ForDashboard();

    PurchaseRecommendation findByProduct(Long productId);

    /**
     * Compara stock actual contra demanda predicha y clasifica el riesgo
     * de agotamiento (LOW/MEDIUM/HIGH). Lógica encapsulada aparte para
     * poder modificarla sin tocar el resto del motor de recomendación.
     */
    RiskLevel calculateStockoutRisk(Long productId);

    /**
     * Genera (o regenera) la recomendación de compra de un producto,
     * combinando demanda predicha, stock, rotación, riesgo y anomalías.
     */
    PurchaseRecommendation generateRecommendation(Long productId);
    
}