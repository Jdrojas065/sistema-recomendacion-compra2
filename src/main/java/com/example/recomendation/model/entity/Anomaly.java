package com.example.recomendation.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "anomalies", indexes = {
        @Index(name = "idx_anomaly_product", columnList = "product_id"),
        @Index(name = "idx_anomaly_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "expected_value", precision = 12, scale = 2)
    private BigDecimal expectedValue;

    @Column(name = "observed_value", precision = 12, scale = 2)
    private BigDecimal observedValue;

    @Column(precision = 6, scale = 4)
    private BigDecimal deviation;

    @Column(length = 20)
    private String severity; // ej. LOW / MEDIUM / HIGH

    @Column(length = 255)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}