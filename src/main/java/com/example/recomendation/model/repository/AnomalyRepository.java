package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.Anomaly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

    @Query(value = "SELECT a FROM Anomaly a JOIN FETCH a.product ORDER BY a.date DESC",
            countQuery = "SELECT COUNT(a) FROM Anomaly a")
    Page<Anomaly> findAllByOrderByDateDesc(Pageable pageable);

    @Query(value = "SELECT a FROM Anomaly a JOIN FETCH a.product WHERE a.product.id = :productId ORDER BY a.date DESC",
            countQuery = "SELECT COUNT(a) FROM Anomaly a WHERE a.product.id = :productId")
    Page<Anomaly> findByProductIdOrderByDateDesc(Long productId, Pageable pageable);

    long countBySeverity(String severity);
    
    @Query("""
            SELECT a FROM Anomaly a
            JOIN FETCH a.product
            ORDER BY a.date DESC
            LIMIT 5
            """)
    List<Anomaly> findTop5ByOrderByDateDesc();
    
    // Usado por RecommendationService para saber si un producto tuvo alguna
    // anomalía reciente y, si es así, subir la prioridad de su recomendación.
    boolean existsByProductIdAndDateAfter(Long productId, java.time.LocalDateTime after);
}