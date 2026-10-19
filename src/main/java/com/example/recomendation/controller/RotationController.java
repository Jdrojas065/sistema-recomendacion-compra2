package com.example.recomendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.recomendation.model.service.interfaces.ProductService;
import com.example.recomendation.model.service.interfaces.RotationService;

@Controller
@RequiredArgsConstructor
public class RotationController {

    private final RotationService rotationService;
    private final ProductService productService;

    @GetMapping("/inventory/rotation/{productId}")
    public String rotation(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.findById(productId));
        model.addAttribute("rotation", rotationService.calculate(productId));
        return "inventory/rotacion";
    }
}