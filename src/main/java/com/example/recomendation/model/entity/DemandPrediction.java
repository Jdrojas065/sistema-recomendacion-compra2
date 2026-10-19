package com.example.recomendation.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_predictions", indexes = {
        @Index(name = "idx_prediction_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @Column(name = "prediction_period", length = 50)
    private String predictionPeriod;

    @Column(name = "traditional_prediction", precision = 12, scale = 2)
    private BigDecimal traditionalPrediction;

    @Column(name = "ml_prediction", precision = 12, scale = 2)
    private BigDecimal mlPrediction;

    @Column(name = "selected_prediction", precision = 12, scale = 2)
    private BigDecimal selectedPrediction;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}