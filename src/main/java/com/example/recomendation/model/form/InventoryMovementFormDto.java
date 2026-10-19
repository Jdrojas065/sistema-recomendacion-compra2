package com.example.recomendation.model.form;

import com.example.recomendation.model.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryMovementFormDto {

    @NotNull(message = "Debe seleccionar un producto")
    private Long productId;

    @NotNull(message = "Debe indicar el tipo de movimiento")
    private MovementType type;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Integer quantity;

    // Fecha real en que ocurrió el movimiento (llegada del pedido, por ejemplo).
    // Si no se indica, se asume el día de hoy.
    private LocalDate movementDate;

    // Opcional — si se indica en una entrada (IN), se crea o actualiza el Lote correspondiente.
    private String lotNumber;

    // Solo aplica a entradas (IN) con lote — fecha de vencimiento del lote.
    private LocalDate expirationDate;

    private String description;
}