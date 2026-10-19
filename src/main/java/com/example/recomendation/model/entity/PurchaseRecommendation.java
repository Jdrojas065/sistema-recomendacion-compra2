package com.example.recomendation.model.entity;

import com.example.recomendation.model.enums.RiskLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_recommendations", indexes = {
        @Index(name = "idx_recommendation_product", columnList = "product_id"),
        @Index(name = "idx_recommendation_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(name = "predicted_demand", precision = 12, scale = 2)
    private BigDecimal predictedDemand;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(length = 10)
    private String rotation; // HIGH / LOW

    @Enumerated(EnumType.STRING)
    @Column(name = "stockout_risk", length = 20)
    private RiskLevel stockoutRisk;

    @Column(name = "recommended_quantity")
    private Integer recommendedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RiskLevel priority;

    @Column(length = 500)
    private String reason;

    @Column(length = 30)
    private String source; // ej. TRADITIONAL / ML

    @Column(length = 20)
    private String status; // ej. PENDING / REVIEWED
}