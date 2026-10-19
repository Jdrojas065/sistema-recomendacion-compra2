package com.example.recomendation.controller;

import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.form.ProductFormDto;
import com.example.recomendation.model.mapper.ProductMapper;
import com.example.recomendation.model.repository.CategoryRepository;
import com.example.recomendation.model.service.interfaces.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId,
                        Model model) {
        model.addAttribute("productPage", productService.findAll(PageRequest.of(page, size), name, categoryId));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("filterName", name);
        model.addAttribute("filterCategoryId", categoryId);
        return "products/listar";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/ver";
    }

    @GetMapping("/api/by-barcode")
    @ResponseBody
    public ResponseEntity<Product> findByBarcode(@RequestParam String code) {
        try {
            return ResponseEntity.ok(productService.findByBarcode(code));
        } catch (com.example.recomendation.exception.ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        ProductFormDto form = new ProductFormDto();
        form.setInitialStock(0);
        form.setEntryDate(java.time.LocalDate.now());
        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/crear";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductFormDto form,
                          BindingResult result,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "products/crear";
        }
        try {
            productService.create(form, authentication.getName());
        } catch (com.example.recomendation.exception.DuplicateBarcodeException ex) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("barcodeError", ex.getMessage());
            return "products/crear";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Producto creado correctamente");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("productForm", ProductMapper.toForm(productService.findById(id)));
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/editar";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("productForm") ProductFormDto form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "products/editar";
        }
        try {
            productService.update(id, form);
        } catch (com.example.recomendation.exception.DuplicateBarcodeException ex) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("barcodeError", ex.getMessage());
            return "products/editar";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Producto actualizado correctamente");
        return "redirect:/products";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Producto dado de baja correctamente");
        return "redirect:/products";
    }
}