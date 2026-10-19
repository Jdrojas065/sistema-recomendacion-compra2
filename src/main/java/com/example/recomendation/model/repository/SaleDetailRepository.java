package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    List<SaleDetail> findBySaleId(Long saleId);

    // Usado por el método tradicional de predicción (historial de ventas de un producto).
    @org.springframework.data.jpa.repository.Query(
            "SELECT sd FROM SaleDetail sd WHERE sd.product.id = :productId " +
            "AND sd.sale.date BETWEEN :start AND :end ORDER BY sd.sale.date ASC")
    List<SaleDetail> findByProductIdAndDateRange(
            @org.springframework.data.repository.query.Param("productId") Long productId,
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end);

    // Usado por MachineLearningClient para calcular "días desde la última venta" (feature del modelo).
    @org.springframework.data.jpa.repository.Query(
            "SELECT MAX(sd.sale.date) FROM SaleDetail sd WHERE sd.product.id = :productId")
    LocalDateTime findLastSaleDate(@org.springframework.data.repository.query.Param("productId") Long productId);

    // Usado por StockProjectionService para traer TODO el historial relevante
    // de una sola vez, en vez de una consulta por producto (evita N+1).
    // JOIN FETCH: trae la Sale asociada ya resuelta en la misma consulta,
    // para no dejarla como proxy perezoso (evita LazyInitializationException).
    @org.springframework.data.jpa.repository.Query(
            "SELECT sd FROM SaleDetail sd JOIN FETCH sd.sale WHERE sd.sale.date BETWEEN :start AND :end")
    List<SaleDetail> findAllByDateRange(
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end);
}