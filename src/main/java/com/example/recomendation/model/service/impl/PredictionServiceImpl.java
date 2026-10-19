package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.integration.ml.MachineLearningClient;
import com.example.recomendation.integration.ml.dto.MLPredictionRequest;
import com.example.recomendation.integration.ml.dto.MLPredictionResponse;
import com.example.recomendation.model.entity.DemandPrediction;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.SaleDetail;
import com.example.recomendation.model.repository.DemandPredictionRepository;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.repository.SaleDetailRepository;
import com.example.recomendation.model.service.interfaces.PredictionService;
import com.example.recomendation.model.service.interfaces.RotationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final ProductRepository productRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final DemandPredictionRepository demandPredictionRepository;
    private final RotationService rotationService;
    private final MachineLearningClient machineLearningClient;

    // Mínimo de puntos de datos para considerar el promedio móvil confiable.
    private static final int MIN_DATA_POINTS = 3;

    @Override
    public TraditionalPredictionResult predictTraditional(Long productId, int periods) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(periods);

        List<SaleDetail> details = saleDetailRepository.findByProductIdAndDateRange(productId, start, end);

        boolean sufficientData = details.size() >= MIN_DATA_POINTS;

        BigDecimal average = BigDecimal.ZERO;
        if (!details.isEmpty()) {
            int totalSold = details.stream().mapToInt(SaleDetail::getQuantity).sum();
            average = BigDecimal.valueOf(totalSold)
                    .divide(BigDecimal.valueOf(details.size()), 2, RoundingMode.HALF_UP);
        }

        // El promedio se calcula igual aunque haya pocos datos (útil como feature para ML);
        // sufficientData solo indica si el propio método tradicional lo considera confiable
        // para mostrarlo como predicción oficial en pantalla.
        return new TraditionalPredictionResult(
                productId, periods, details.size(), average, sufficientData, "MOVING_AVERAGE");
    }

    @Override
    public ComparisonResult compare(Long productId) {
        TraditionalPredictionResult traditional = predictTraditional(productId, 6);

        Product product = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        MLPredictionRequest mlRequest = new MLPredictionRequest(
                productId,
                LocalDate.now().getDayOfWeek().getValue(),
                LocalDate.now().getMonthValue(),
                mapRotationToCode(rotationService.calculate(productId).classification()),
                product.getCurrentStock(),
                traditional.estimatedDemand().doubleValue(),
                product.getCategory().getId(),
                daysSinceLastSale(productId)
        );

        MLPredictionResponse mlResponse = machineLearningClient.predict(mlRequest);

        boolean mlAvailable = mlResponse != null;
        BigDecimal mlPrediction = mlAvailable
                ? BigDecimal.valueOf(mlResponse.predictedDemand()).setScale(2, RoundingMode.HALF_UP)
                : null;

        return new ComparisonResult(
                productId, traditional.estimatedDemand(), traditional.sufficientData(), mlPrediction, mlAvailable);
    }

    @Override
    @Transactional
    public DemandPrediction saveComparisonResult(Long productId, ComparisonResult result) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        BigDecimal selected = result.mlAvailable() ? result.mlPrediction() : result.traditionalPrediction();
        String modelUsed = result.mlAvailable() ? "ML" : "TRADITIONAL";

        DemandPrediction prediction = DemandPrediction.builder()
                .product(product)
                .predictionDate(LocalDate.now())
                .predictionPeriod("6")
                .traditionalPrediction(result.traditionalPrediction())
                .mlPrediction(result.mlPrediction())
                .selectedPrediction(selected)
                .modelUsed(modelUsed)
                .build();

        return demandPredictionRepository.save(prediction);
    }

    /** RotationService en este proyecto solo clasifica HIGH/LOW (sin nivel medio). */
    private int mapRotationToCode(String rotation) {
        return "HIGH".equalsIgnoreCase(rotation) ? 1 : 0;
    }

    private long daysSinceLastSale(Long productId) {
        LocalDateTime lastSale = saleDetailRepository.findLastSaleDate(productId);
        if (lastSale == null) {
            return 9999; // nunca se ha vendido: valor grande, deja que el modelo lo interprete como "sin actividad"
        }
        return ChronoUnit.DAYS.between(lastSale.toLocalDate(), LocalDate.now());
    }
}