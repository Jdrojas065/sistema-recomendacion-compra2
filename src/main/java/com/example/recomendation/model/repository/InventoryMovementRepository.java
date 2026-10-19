package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // Usado por inventory/historial.html (accede a m.user.name).
    @Query(value = "SELECT m FROM InventoryMovement m JOIN FETCH m.user " +
            "WHERE m.product.id = :productId ORDER BY m.date DESC",
            countQuery = "SELECT COUNT(m) FROM InventoryMovement m WHERE m.product.id = :productId")
    Page<InventoryMovement> findByProductIdOrderByDateDesc(Long productId, Pageable pageable);

    @Query(value = "SELECT m FROM InventoryMovement m JOIN FETCH m.user " +
            "WHERE m.product.id = :productId AND m.date BETWEEN :start AND :end ORDER BY m.date DESC",
            countQuery = "SELECT COUNT(m) FROM InventoryMovement m " +
                    "WHERE m.product.id = :productId AND m.date BETWEEN :start AND :end")
    Page<InventoryMovement> findByProductIdAndDateBetweenOrderByDateDesc(
            Long productId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}