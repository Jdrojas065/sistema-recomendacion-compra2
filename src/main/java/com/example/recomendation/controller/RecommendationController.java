package com.example.recomendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.recomendation.model.service.interfaces.ProductService;
import com.example.recomendation.model.service.interfaces.RecommendationService;

@Controller
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ProductService productService;
    private final com.example.recomendation.model.repository.PurchaseRecommendationRepository purchaseRecommendationRepository;
    private final com.example.recomendation.model.service.interfaces.StockProjectionService stockProjectionService;
    private final com.example.recomendation.model.repository.ProductRepository productRepository;
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        // Genera automáticamente la recomendación de cualquier producto activo
        // que todavía no tenga una (por ejemplo, si nunca tuvo una venta que
        // disparara el cálculo). Así el listado siempre muestra el catálogo completo.
        productRepository.findAll().stream()
                .filter(com.example.recomendation.model.entity.Product::getActive)
                .filter(p -> !purchaseRecommendationRepository.existsByProductId(p.getId()))
                .forEach(p -> recommendationService.generateRecommendation(p.getId()));

        model.addAttribute("recommendationPage", recommendationService.findAll(PageRequest.of(page, size)));
        model.addAttribute("highCount", purchaseRecommendationRepository.countByPriority(com.example.recomendation.model.enums.RiskLevel.HIGH));
        model.addAttribute("mediumCount", purchaseRecommendationRepository.countByPriority(com.example.recomendation.model.enums.RiskLevel.MEDIUM));
        model.addAttribute("lowCount", purchaseRecommendationRepository.countByPriority(com.example.recomendation.model.enums.RiskLevel.LOW));
        return "recommendations/listar";
    }
    @GetMapping("/early-warnings")
    public String earlyWarnings(Model model) {
        model.addAttribute("warnings", stockProjectionService.findEarlyWarnings());
        return "recommendations/alertas-tempranas";
    }

    @GetMapping("/{productId}")
    public String detail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("recommendation", recommendationService.findByProduct(productId));
        return "recommendations/ver";
    }

    @org.springframework.web.bind.annotation.PostMapping("/{productId}/generate")
    public String regenerate(@PathVariable Long productId,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        recommendationService.generateRecommendation(productId);
        redirectAttributes.addFlashAttribute("successMessage", "Recomendación recalculada.");
        return "redirect:/recommendations/" + productId;
    }

    @GetMapping("/stockout-risk/{productId}")
    public String stockoutRisk(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("risk", recommendationService.calculateStockoutRisk(productId));
        return "recommendations/stockout-risk";
    }
}