package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category
            WHERE p.active = true
            """)
    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category
            WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category
            WHERE p.active = true AND p.category.id = :categoryId
            """)
    Page<Product> findByActiveTrueAndCategoryId(Long categoryId, Pageable pageable);
    
    List<Product> findByActiveTrue();

    // Usado por products/ver.html (accede a product.category.name).
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(Long id);

    Optional<Product> findByBarcodeAndActiveTrue(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);
    

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND p.currentStock <= p.minimumStock")
    List<Product> findLowStockProducts();
}