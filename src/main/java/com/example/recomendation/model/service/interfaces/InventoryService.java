package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.InventoryMovement;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.form.InventoryMovementFormDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    Page<Product> findStock(Pageable pageable, Long categoryId);

    Product findStockByProduct(Long productId);

    /**
     * Registra un movimiento (IN/OUT) y actualiza el stock del producto
     * dentro de una transacción. Lanza BusinessException si intenta
     * dejar el stock negativo, y OptimisticLockConflictException si
     * hay un conflicto de concurrencia sobre el producto.
     */
    InventoryMovement registerMovement(InventoryMovementFormDto form, String username);

    Page<InventoryMovement> findHistoryByProduct(Long productId, Pageable pageable);
}