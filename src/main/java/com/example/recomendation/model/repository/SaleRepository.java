package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query(value = "SELECT s FROM Sale s JOIN FETCH s.user WHERE s.user.id = :userId ORDER BY s.date DESC",
            countQuery = "SELECT COUNT(s) FROM Sale s WHERE s.user.id = :userId")
    Page<Sale> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT s FROM Sale s JOIN FETCH s.user WHERE s.date BETWEEN :start AND :end ORDER BY s.date DESC",
            countQuery = "SELECT COUNT(s) FROM Sale s WHERE s.date BETWEEN :start AND :end")
    Page<Sale> findByDateBetweenOrderByDateDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query(value = "SELECT s FROM Sale s JOIN FETCH s.user ORDER BY s.date DESC",
            countQuery = "SELECT COUNT(s) FROM Sale s")
    Page<Sale> findAllByOrderByDateDesc(Pageable pageable);

    @Query("SELECT s FROM Sale s JOIN FETCH s.user WHERE s.id = :id")
    java.util.Optional<Sale> findByIdWithUser(Long id);

    // Usado por la vista de detalle (sales/ver.html), que recorre sale.details y d.product.
    // DISTINCT evita filas duplicadas de Sale por el join con la colección de detalles.
    @Query("SELECT DISTINCT s FROM Sale s " +
            "JOIN FETCH s.user " +
            "LEFT JOIN FETCH s.details d " +
            "LEFT JOIN FETCH d.product " +
            "WHERE s.id = :id")
    java.util.Optional<Sale> findByIdWithDetails(Long id);
    
    List<Sale> findByDateBetween(LocalDateTime start, LocalDateTime end);
}