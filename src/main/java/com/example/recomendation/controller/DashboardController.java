package com.example.recomendation.controller;

import com.example.recomendation.model.entity.Anomaly;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.PurchaseRecommendation;
import com.example.recomendation.model.repository.AnomalyRepository;
import com.example.recomendation.model.service.interfaces.ProductService;
import com.example.recomendation.model.service.interfaces.RecommendationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;
    private final RecommendationService recommendationService;
    private final AnomalyRepository anomalyRepository;
    private final com.example.recomendation.model.service.interfaces.StockProjectionService stockProjectionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> lowStockProducts = productService.findLowStock();

        List<PurchaseRecommendation> topRecommendations = recommendationService.findTop5ForDashboard();

        List<Anomaly> recentAnomalies = anomalyRepository.findTop5ByOrderByDateDesc();

        long highSeverityAnomalies = anomalyRepository.countBySeverity("HIGH");

        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("topRecommendations", topRecommendations);
        model.addAttribute("recentAnomalies", recentAnomalies);
        model.addAttribute("highSeverityAnomaliesCount", highSeverityAnomalies);
        model.addAttribute("earlyWarningsCount", stockProjectionService.findEarlyWarnings().size());

        return "dashboard/index";
    }
}