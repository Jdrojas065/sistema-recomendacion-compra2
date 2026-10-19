package com.example.recomendation.security.mfa;

import org.springframework.security.core.AuthenticationException;

public class MfaRequiredException extends AuthenticationException {

    private final String email;

    public MfaRequiredException(String email) {
        super("Se requiere verificación MFA");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}