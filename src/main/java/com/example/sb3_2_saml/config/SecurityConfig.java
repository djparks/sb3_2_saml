package com.example.sb3_2_saml.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Spring Security configuration.
 *
 * Key points:
 * - Permits health checks and public APIs.
 * - Protects all other endpoints, requiring authentication.
 * - Enables SAML2 login to support SSO with an external IdP.
 * - Disables CSRF for /api/** paths (stateless API best practice) while keeping it enabled elsewhere.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/saml/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .saml2Login(saml -> saml.successHandler(authenticationSuccessHandler()))
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChainSimple(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/samlsimple/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }
}
