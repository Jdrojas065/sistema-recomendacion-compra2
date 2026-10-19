package com.example.recomendation.controller;

import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.repository.UserRepository;
import com.example.recomendation.security.mfa.RecoveryCodeService;
import com.example.recomendation.security.mfa.TotpService;
import com.example.recomendation.security.service.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mfa")
@RequiredArgsConstructor
public class MfaChallengeController {

    // Tras este numero de intentos fallidos (TOTP o codigo de recuperacion,
    // cuentan juntos), se invalida la sesion y se obliga a iniciar sesion
    // de nuevo con contrasena. Protege contra fuerza bruta sobre el codigo TOTP.
    private static final int MAX_ATTEMPTS = 5;
    private static final String ATTR_PENDING_EMAIL = "MFA_PENDING_EMAIL";
    private static final String ATTR_FAILED_ATTEMPTS = "MFA_FAILED_ATTEMPTS";

    private final UserRepository userRepository;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/challenge")
    public String show(HttpSession session) {
        if (session.getAttribute(ATTR_PENDING_EMAIL) == null) return "redirect:/login";
        return "mfa/challenge";
    }

    @PostMapping("/challenge")
    public String verify(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        String email = (String) request.getSession().getAttribute(ATTR_PENDING_EMAIL);
        if (email == null) return "redirect:/login";

        User user = userRepository.findByEmail(email).orElseThrow();
        if (!totpService.verify(user.getMfaSecret(), code)) {
            return registerFailedAttempt(request);
        }
        return completeLogin(user, request, response);
    }

    @PostMapping("/challenge/recovery")
    public String verifyWithRecoveryCode(@RequestParam String recoveryCode,
                                          HttpServletRequest request, HttpServletResponse response) {
        String email = (String) request.getSession().getAttribute(ATTR_PENDING_EMAIL);
        if (email == null) return "redirect:/login";

        User user = userRepository.findByEmail(email).orElseThrow();
        if (!recoveryCodeService.verifyAndConsume(user, recoveryCode)) {
            return registerFailedAttempt(request);
        }
        return completeLogin(user, request, response);
    }

    /**
     * Incrementa el contador de intentos fallidos en sesion. Al llegar al maximo,
     * invalida la sesion por completo (obliga a re-ingresar usuario y contrasena).
     */
    private String registerFailedAttempt(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer current = (Integer) session.getAttribute(ATTR_FAILED_ATTEMPTS);
        int attempts = (current != null ? current : 0) + 1;

        if (attempts >= MAX_ATTEMPTS) {
            session.invalidate();
            return "redirect:/login?mfaLocked";
        }

        session.setAttribute(ATTR_FAILED_ATTEMPTS, attempts);
        return "redirect:/mfa/challenge?error";
    }

    private String completeLogin(User user, HttpServletRequest request, HttpServletResponse response) {
        UserPrincipal principal = new UserPrincipal(user);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        try {
            sessionAuthenticationStrategy.onAuthentication(auth, request, response);
        } catch (SessionAuthenticationException e) {
            request.getSession().removeAttribute(ATTR_PENDING_EMAIL);
            return "redirect:/login?error";
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        request.getSession().removeAttribute(ATTR_PENDING_EMAIL);
        request.getSession().removeAttribute(ATTR_FAILED_ATTEMPTS);
        return "redirect:/dashboard";
    }
}