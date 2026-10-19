package com.example.recomendation.controller;

import com.example.recomendation.model.form.UserFormDto;
import com.example.recomendation.model.mapper.UserMapper;
import com.example.recomendation.model.service.interfaces.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        model.addAttribute("userPage", userService.findAll(PageRequest.of(page, size)));
        return "users/listar";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("userForm", new UserFormDto());
        return "users/crear";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserFormDto form,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/crear";
        }
        userService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario creado correctamente");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", UserMapper.toForm(userService.findById(id)));
        return "users/editar";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("userForm") UserFormDto form,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/editar";
        }
        userService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");
        return "redirect:/users";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario dado de baja correctamente");
        return "redirect:/users";
    }
}