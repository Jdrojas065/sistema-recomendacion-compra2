package com.example.recomendation.integration.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload enviado al servicio Python (POST /predict).
 * Los nombres de campo usan snake_case explícito vía @JsonProperty porque
 * el servicio Python (FastAPI/Pydantic) espera ese formato, no camelCase.
 */
public record MLPredictionRequest(

        @JsonProperty("product_id")
        Long productId,

        @JsonProperty("day_of_week")
        int dayOfWeek,

        @JsonProperty("month")
        int month,

        @JsonProperty("rotation_code")
        int rotationCode,          // 0 = LOW, 1 = HIGH

        @JsonProperty("current_stock")
        int currentStock,

        @JsonProperty("avg_demand_last_3")
        double avgDemandLast3,

        @JsonProperty("category_id")
        Long categoryId,

        @JsonProperty("days_since_last_sale")
        long daysSinceLastSale
) {}