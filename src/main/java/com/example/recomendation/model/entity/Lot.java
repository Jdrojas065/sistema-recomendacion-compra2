package com.example.recomendation.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lots", indexes = {
        @Index(name = "idx_lot_product", columnList = "product_id"),
        @Index(name = "idx_lot_number", columnList = "lot_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    // Opcional — no todos los productos tienen fecha de vencimiento.
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}