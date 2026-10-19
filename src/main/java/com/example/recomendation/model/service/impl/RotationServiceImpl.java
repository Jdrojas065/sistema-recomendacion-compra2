package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.SaleDetail;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.repository.SaleDetailRepository;
import com.example.recomendation.model.service.interfaces.RotationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RotationServiceImpl implements RotationService {

    // Umbral simple: por encima de este valor se considera rotación ALTA.
    // Documentado aquí para facilitar el ajuste posterior sin tocar el resto del sistema.
    private static final double HIGH_ROTATION_THRESHOLD = 1.0;
    private static final int ANALYSIS_MONTHS = 3;

    private final ProductRepository productRepository;
    private final SaleDetailRepository saleDetailRepository;

    @Override
    public RotationResult calculate(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(ANALYSIS_MONTHS);

        List<SaleDetail> details = saleDetailRepository.findByProductIdAndDateRange(productId, start, end);
        int totalSold = details.stream().mapToInt(SaleDetail::getQuantity).sum();

        // Fórmula simple: unidades vendidas en el periodo / stock promedio (actual como aproximación).
        // Preparada para reemplazarse por una fórmula más elaborada más adelante.
        double averageStock = Math.max(product.getCurrentStock(), 1);
        double rotationValue = totalSold / averageStock;

        String classification = rotationValue >= HIGH_ROTATION_THRESHOLD ? "HIGH" : "LOW";

        return new RotationResult(productId, rotationValue, classification);
    }
}