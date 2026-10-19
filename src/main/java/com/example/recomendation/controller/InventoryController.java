package com.example.recomendation.controller;

import com.example.recomendation.model.form.InventoryMovementFormDto;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.service.interfaces.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    @GetMapping("/stock")
    public String stock(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(required = false) Long categoryId,
                         Model model) {
        model.addAttribute("productPage", inventoryService.findStock(PageRequest.of(page, size), categoryId));
        return "inventory/stock";
    }

    @GetMapping("/stock/{productId}")
    public String stockDetail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", inventoryService.findStockByProduct(productId));
        return "inventory/stock-detalle";
    }

    @GetMapping("/movements/new")
    public String newMovementForm(Model model) {
        model.addAttribute("movementForm", new InventoryMovementFormDto());
        model.addAttribute("products", productRepository.findAll());
        return "inventory/movimiento";
    }

    @PostMapping("/movements")
    public String registerMovement(@Valid @ModelAttribute("movementForm") InventoryMovementFormDto form,
                                    BindingResult result,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findAll());
            return "inventory/movimiento";
        }
        // BusinessException (stock insuficiente) y OptimisticLockConflictException
        // se resuelven automáticamente en error/generic.html vía GlobalExceptionHandler.
        inventoryService.registerMovement(form, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Movimiento registrado correctamente");
        return "redirect:/inventory/stock";
    }

    @GetMapping("/history/{productId}")
    public String history(@PathVariable Long productId,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model) {
        model.addAttribute("product", inventoryService.findStockByProduct(productId));
        model.addAttribute("movementPage", inventoryService.findHistoryByProduct(productId, PageRequest.of(page, size)));
        return "inventory/historial";
    }
}