package com.example.recomendation.controller;

import com.example.recomendation.model.repository.AnomalyRepository;
import com.example.recomendation.model.service.interfaces.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyRepository anomalyRepository;
    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        model.addAttribute("anomalyPage", anomalyRepository.findAllByOrderByDateDesc(PageRequest.of(page, size)));
        model.addAttribute("highCount", anomalyRepository.countBySeverity("HIGH"));
        model.addAttribute("mediumCount", anomalyRepository.countBySeverity("MEDIUM"));
        model.addAttribute("lowCount", anomalyRepository.countBySeverity("LOW"));
        return "anomalies/listar";
    }

    @GetMapping("/{productId}")
    public String byProduct(@PathVariable Long productId,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("anomalyPage", anomalyRepository.findByProductIdOrderByDateDesc(productId, PageRequest.of(page, size)));
        return "anomalies/listar";
    }
}