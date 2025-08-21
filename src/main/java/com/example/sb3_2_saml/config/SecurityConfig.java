package com.example.sb3_2_saml.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Spring Security configuration.
 *
 * Notes on this setup:
 * - We define multiple SecurityFilterChain beans to handle different path segments.
 *   Spring Security picks the first chain that matches the request. Adding @Order provides
 *   explicit, deterministic ordering. More specific paths should have a lower order value. 
 * - The /saml/** chain enables SAML2 login and handles SSO-related endpoints.
 * - The /samlsimple/** chain demonstrates a simpler, non-SAML chain for a different endpoint group.
 * - CSRF: ignoring /api/** inside these path-scoped chains will only have effect if the request
 *   also matches the chain's securityMatcher. As written, /api/** does not match either matcher,
 *   so this CSRF setting is effectively a no-op here.
 *
 * Suggestions (non-functional, TODO):
 * - Consider adding a catch-all chain (no securityMatcher) for the rest of the application, where
 *   you can centralize common rules and CSRF configuration for API vs. browser endpoints.
 * - If you expose stateless REST APIs under /api/**, create a dedicated chain with:
 *     .securityMatcher("/api/**")
 *     .csrf(csrf -> csrf.disable())
 *     .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 *     and appropriate authentication (e.g. JWT or Basic), separate from SAML.
 * - To reduce duplication, extract shared authorization rules into a helper method.
 */
@Configuration
public class SecurityConfig {

    /**
     * SAML-enabled security chain for /saml/** endpoints.
     *
     * Ordering: 1 (evaluated before less specific chains).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Only applies to requests under /saml/**
            .securityMatcher("/saml/**")
            // Public endpoints commonly allowed for health checks and anonymous APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            // Note: This CSRF ignore only applies within this chain's matcher (/saml/**),
            // so ignoring /api/** here likely has no effect. See class-level suggestions.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            // Enable SAML2 login and use a success handler that honors saved requests
            .saml2Login(saml -> saml.successHandler(authenticationSuccessHandler()))
            // Simple logout redirect to home
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    /**
     * Simple chain for /samlsimple/** endpoints.
     *
     * Ordering: 2 (evaluated after /saml/** chain).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainSimple(HttpSecurity http) throws Exception {
        http
                // Applies to requests under /samlsimple/**
                .securityMatcher("/samlsimple/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Note: Same as above, this likely has no effect within this chain
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    /**
     * Reusable success handler that redirects to a saved request if present,
     * otherwise falls back to "/".
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }
}
