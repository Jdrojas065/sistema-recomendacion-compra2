package com.example.recomendation.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductFormDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Pattern(regexp = "^$|^[0-9]{6,20}$", message = "El código de barras debe tener solo números (6 a 20 dígitos)")
    private String barcode;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    @NotNull(message = "Debe seleccionar una categoría")
    private Long categoryId;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock;

    @PositiveOrZero(message = "El stock inicial no puede ser negativo")
    private Integer initialStock;

    private LocalDate entryDate;

    private String lotNumber;

    private LocalDate expirationDate;
}