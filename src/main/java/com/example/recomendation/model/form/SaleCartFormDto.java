package com.example.recomendation.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un ítem dentro del carrito de venta que se mantiene
 * en la sesión HTTP mientras el usuario arma la venta (ver sección 9 del prompt).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleCartFormDto {

    @NotNull(message = "Debe seleccionar un producto")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Integer quantity;

    // Campos de solo lectura, poblados por el servicio al agregar el ítem,
    // usados únicamente para mostrar el carrito en la vista (no se validan en el submit).
    private String productName;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal subtotal;
}