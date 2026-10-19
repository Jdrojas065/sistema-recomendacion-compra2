package com.example.recomendation.controller;

import com.example.recomendation.model.form.CategoryFormDto;
import com.example.recomendation.model.mapper.CategoryMapper;
import com.example.recomendation.model.service.interfaces.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        model.addAttribute("categoryPage", categoryService.findAll(PageRequest.of(page, size)));
        return "categories/listar";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryFormDto());
        return "categories/crear";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryFormDto form,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "categories/crear";
        }
        categoryService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Categoría creada correctamente");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", CategoryMapper.toForm(categoryService.findById(id)));
        return "categories/editar";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("categoryForm") CategoryFormDto form,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "categories/editar";
        }
        categoryService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Categoría actualizada correctamente");
        return "redirect:/categories";
    }
}