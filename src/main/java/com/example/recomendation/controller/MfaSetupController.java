package com.example.recomendation.controller;

import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.repository.UserRepository;
import com.example.recomendation.security.mfa.RecoveryCodeService;
import com.example.recomendation.security.mfa.TotpService;
import com.example.recomendation.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile/mfa")
@RequiredArgsConstructor
public class MfaSetupController {

    private final UserRepository userRepository;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;

    @GetMapping("/setup")
    public String setup(Model model, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        String secret = totpService.generateSecret();
        model.addAttribute("qrImage", totpService.generateQrCodeImage(user.getEmail(), secret));
        model.addAttribute("secret", secret);
        return "profile/mfa-setup";
    }

    @PostMapping("/enable")
    public String enable(@RequestParam String secret, @RequestParam String code,
                          Authentication authentication, RedirectAttributes ra) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        if (!totpService.verify(secret, code)) {
            ra.addFlashAttribute("error", "Código incorrecto");
            return "redirect:/profile/mfa/setup";
        }
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userRepository.save(user);

        List<String> recoveryCodes = recoveryCodeService.regenerate(user);
        ra.addFlashAttribute("recoveryCodes", recoveryCodes);
        return "redirect:/profile/mfa/recovery-codes";
    }
    @PostMapping("/disable")
    public String disable(@RequestParam String code, Authentication authentication, RedirectAttributes ra) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();

        // Exige el código TOTP actual antes de desactivar — evita que una sesión
        // dejada abierta (equipo compartido, robo de sesión) pueda quitar el MFA
        // sin demostrar que quien lo hace todavía tiene el segundo factor.
        if (!totpService.verify(user.getMfaSecret(), code)) {
            ra.addFlashAttribute("error", "Código incorrecto. No se desactivó MFA.");
            return "redirect:/profile";
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
        return "redirect:/profile";
    }

    @GetMapping("/recovery-codes")
    public String showRecoveryCodes(Model model) {
        if (!model.containsAttribute("recoveryCodes")) {
            return "redirect:/profile"; // evita mostrar la pantalla si no se acaba de generar un lote
        }
        return "profile/mfa-recovery-codes";
    }

    @PostMapping("/recovery-codes/regenerate")
    public String regenerateRecoveryCodes(Authentication authentication, RedirectAttributes ra) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        ra.addFlashAttribute("recoveryCodes", recoveryCodeService.regenerate(user));
        return "redirect:/profile/mfa/recovery-codes";
    }
}