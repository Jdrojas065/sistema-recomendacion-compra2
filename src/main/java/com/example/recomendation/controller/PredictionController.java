package com.example.recomendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.recomendation.model.service.interfaces.PredictionService;
import com.example.recomendation.model.service.interfaces.ProductService;

import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final ProductService productService;

    @GetMapping("/traditional/{productId}")
    public String traditional(@PathVariable Long productId,
                               @RequestParam(defaultValue = "6") int periods,
                               Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("result", predictionService.predictTraditional(productId, periods));
        return "predictions/tradicional";
    }

    @GetMapping("/compare/{productId}")
    public String compare(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("comparison", predictionService.compare(productId));
        return "predictions/comparacion";
    }
}