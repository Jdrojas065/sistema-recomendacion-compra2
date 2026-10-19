package com.example.recomendation.integration.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta del servicio Python (POST /predict). */
public record MLPredictionResponse(

        @JsonProperty("product_id")
        Long productId,

        @JsonProperty("predicted_demand")
        double predictedDemand,

        @JsonProperty("model_used")
        String modelUsed
) {}