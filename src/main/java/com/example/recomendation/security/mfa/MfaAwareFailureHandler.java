package com.example.recomendation.security.mfa;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MfaAwareFailureHandler implements AuthenticationFailureHandler {

    private final AuthenticationFailureHandler fallback = new SimpleUrlAuthenticationFailureHandler("/login?error");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof MfaRequiredException mfaEx) {
            request.getSession().setAttribute("MFA_PENDING_EMAIL", mfaEx.getEmail());
            response.sendRedirect("/mfa/challenge");
            return;
        }
        fallback.onAuthenticationFailure(request, response, exception);
    }
}