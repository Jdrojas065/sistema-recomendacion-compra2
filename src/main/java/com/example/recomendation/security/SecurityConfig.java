package com.example.recomendation.security;

import com.example.recomendation.security.mfa.MfaAuthenticationProvider;
import com.example.recomendation.security.mfa.MfaAwareFailureHandler;
import com.example.recomendation.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final MfaAwareFailureHandler mfaAwareFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(MfaAuthenticationProvider mfaAuthenticationProvider) {
        return new ProviderManager(mfaAuthenticationProvider);
    }

    // Registro de sesiones activas por usuario — lo necesita tanto el control
    // de sesión única como el registro/expiración cuando alguien cierra sesión.
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // Sin esto, las sesiones que expiran o se invalidan (timeout, logout)
    // NO se quitan del SessionRegistry, y con el tiempo maximumSessions(1)
    // empieza a contar sesiones "fantasma" que ya no existen.
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Estrategia única de sesión, compartida entre el login normal (formLogin)
    // y el login manual de MfaChallengeController. Antes esto vivía "escondido"
    // dentro de .maximumSessions(1) y solo se ejecutaba en el flujo normal.
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionStrategy =
                new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionStrategy.setMaximumSessions(1);
        concurrentSessionStrategy.setExceptionIfMaximumExceeded(false); // expira la sesión anterior, no bloquea la nueva

        // Resuelve la fijación de sesión: regenera el ID de sesión al autenticar.
        ChangeSessionIdAuthenticationStrategy changeSessionIdStrategy = new ChangeSessionIdAuthenticationStrategy();

        RegisterSessionAuthenticationStrategy registerSessionStrategy =
                new RegisterSessionAuthenticationStrategy(sessionRegistry);

        return new CompositeSessionAuthenticationStrategy(
                List.of(concurrentSessionStrategy, changeSessionIdStrategy, registerSessionStrategy)
        );
    }

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http,
                                               AuthenticationManager authenticationManager,
                                               SessionAuthenticationStrategy sessionAuthenticationStrategy) throws Exception {
        http
            .authenticationManager(authenticationManager)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error/**", "/mfa/challenge/**").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/products/**").hasRole("ADMIN")
                .requestMatchers("/categories/**").hasRole("ADMIN")
                .requestMatchers("/inventory/**").hasRole("ADMIN")
                .requestMatchers("/predictions/**").hasRole("ADMIN")
                .requestMatchers("/recommendations/**").hasRole("ADMIN")
                .requestMatchers("/anomalies/**").hasRole("ADMIN")
                .requestMatchers("/sales/reports").hasRole("ADMIN")
                .requestMatchers("/sales/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(mfaAwareFailureHandler)
                .permitAll()
            )
            .logout(LogoutConfigurer::permitAll)
            .exceptionHandling(handling -> handling
                .accessDeniedPage("/access-denied")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy)
            );

        return http.build();
    }
}