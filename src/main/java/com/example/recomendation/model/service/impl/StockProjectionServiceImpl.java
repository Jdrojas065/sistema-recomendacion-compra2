package com.example.recomendation.model.service.impl;

import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.SaleDetail;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.repository.SaleDetailRepository;
import com.example.recomendation.model.service.interfaces.StockProjectionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockProjectionServiceImpl implements StockProjectionService {

    private static final int RECENT_WINDOW_DAYS = 7;
    private static final int BASELINE_WINDOW_DAYS = 30;
    private static final double ACCELERATION_THRESHOLD = 1.3;
    private static final int WARNING_HORIZON_DAYS = 14;

    private final ProductRepository productRepository;
    private final SaleDetailRepository saleDetailRepository;

    @Override
    public List<StockProjection> findEarlyWarnings() {
        List<Product> activeProducts = productRepository.findByActiveTrue();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentStart = now.minusDays(RECENT_WINDOW_DAYS);
        LocalDateTime baselineStart = now.minusDays(RECENT_WINDOW_DAYS + BASELINE_WINDOW_DAYS);

        // Una sola consulta para TODOS los productos, en vez de 2 por producto.
        List<SaleDetail> allRelevantSales = saleDetailRepository.findAllByDateRange(baselineStart, now);

        Map<Long, List<SaleDetail>> salesByProduct = allRelevantSales.stream()
                .collect(Collectors.groupingBy(sd -> sd.getProduct().getId()));

        List<StockProjection> warnings = new ArrayList<>();

        for (Product product : activeProducts) {
            if (product.getCurrentStock() <= product.getMinimumStock()) {
                continue;
            }

            List<SaleDetail> productSales = salesByProduct.getOrDefault(product.getId(), List.of());

            int recentQty = 0;
            int baselineQty = 0;
            for (SaleDetail sd : productSales) {
                LocalDateTime saleDate = sd.getSale().getDate();
                if (!saleDate.isBefore(recentStart)) {
                    recentQty += sd.getQuantity();
                } else {
                    baselineQty += sd.getQuantity();
                }
            }

            double recentDailyAvg = recentQty / (double) RECENT_WINDOW_DAYS;
            double baselineDailyAvg = baselineQty / (double) BASELINE_WINDOW_DAYS;

            if (recentDailyAvg <= 0) {
                continue;
            }

            long daysUntilMinimum = Math.round(
                    (product.getCurrentStock() - product.getMinimumStock()) / recentDailyAvg);
            long daysUntilStockout = Math.round(product.getCurrentStock() / recentDailyAvg);

            if (daysUntilMinimum > WARNING_HORIZON_DAYS) {
                continue;
            }

            boolean trendAccelerating = baselineDailyAvg > 0
                    && recentDailyAvg >= baselineDailyAvg * ACCELERATION_THRESHOLD;

            LocalDate today = LocalDate.now();
            warnings.add(new StockProjection(
                    product,
                    round2(recentDailyAvg),
                    round2(baselineDailyAvg),
                    trendAccelerating,
                    daysUntilMinimum,
                    daysUntilStockout,
                    today.plusDays(daysUntilMinimum),
                    today.plusDays(daysUntilStockout)
            ));
        }

        warnings.sort((a, b) -> Long.compare(a.daysUntilMinimum(), b.daysUntilMinimum()));
        return warnings;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}