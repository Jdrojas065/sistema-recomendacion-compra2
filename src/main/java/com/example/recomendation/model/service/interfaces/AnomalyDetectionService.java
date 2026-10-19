package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.Product;

public interface AnomalyDetectionService {

    /**
     * Evalúa si la cantidad vendida en una venta recién registrada se aleja
     * significativamente del patrón histórico de ese producto (RF13).
     * Si detecta una anomalía, la guarda. Nunca lanza excepciones hacia
     * arriba: un fallo aquí no debe impedir que la venta se registre.
     */
    void evaluateSale(Product product, int quantitySold);
}