package com.example.recomendation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        // Spring Security intercepta el POST /login automáticamente (ver SecurityConfig).
        // Este método solo renderiza el formulario. Los parámetros ?error y ?logout
        // los lee la propia plantilla login.html con th:if="${param.error}" / ${param.logout}.
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}