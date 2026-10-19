package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.PurchaseRecommendation;
import com.example.recomendation.model.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseRecommendationRepository extends JpaRepository<PurchaseRecommendation, Long> {

    @Query(value = """
            SELECT pr FROM PurchaseRecommendation pr
            JOIN FETCH pr.product
            ORDER BY pr.priority ASC, pr.recommendationDate DESC
            """,
            countQuery = "SELECT COUNT(pr) FROM PurchaseRecommendation pr")
    Page<PurchaseRecommendation> findAllByOrderByPriorityAscRecommendationDateDesc(Pageable pageable);

    @Query("""
            SELECT pr FROM PurchaseRecommendation pr
            JOIN FETCH pr.product
            ORDER BY pr.priority ASC, pr.recommendationDate DESC
            LIMIT 5
            """)
    List<PurchaseRecommendation> findTop5ByOrderByPriorityAscRecommendationDateDesc();

    @Query("SELECT pr FROM PurchaseRecommendation pr JOIN FETCH pr.product WHERE pr.product.id = :productId")
    List<PurchaseRecommendation> findByProductId(Long productId);

    long countByPriority(RiskLevel priority);

    boolean existsByProductId(Long productId);
 // Usado para "upsert": si el producto ya tiene una recomendación, se actualiza
    // en vez de crear una fila nueva cada vez que se recalcula.
    @Query("SELECT pr FROM PurchaseRecommendation pr JOIN FETCH pr.product WHERE pr.product.id = :productId ORDER BY pr.id DESC")
    java.util.List<PurchaseRecommendation> findByProductIdOrderByIdDesc(Long productId);

}
