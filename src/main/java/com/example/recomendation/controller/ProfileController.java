package com.example.recomendation.controller;

import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.repository.UserRepository;
import com.example.recomendation.security.mfa.RecoveryCodeService;
import com.example.recomendation.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final RecoveryCodeService recoveryCodeService;

    @GetMapping
    public String view(Model model, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("recoveryCodesRemaining", recoveryCodeService.countRemaining(user));
        return "profile/view";
    }
}