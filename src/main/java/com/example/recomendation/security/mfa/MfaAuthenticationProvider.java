package com.example.recomendation.security.mfa;

import com.example.recomendation.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MfaAuthenticationProvider implements AuthenticationProvider {

    private final DaoAuthenticationProvider daoAuthenticationProvider; // el bean que ya tienes en SecurityConfig

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication result = daoAuthenticationProvider.authenticate(authentication); // valida correo+password
        UserPrincipal principal = (UserPrincipal) result.getPrincipal();

        if (principal.isMfaEnabled()) {
            throw new MfaRequiredException(principal.getUsername()); // password OK, falta el código TOTP
        }
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return daoAuthenticationProvider.supports(authentication);
    }
}