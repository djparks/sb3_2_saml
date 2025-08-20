package com.example.sb3_2_saml.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoint responds without authentication")
    void publicHello() throws Exception {
        mockMvc.perform(get("/api/public/hello").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Hello from sb3_2_saml!"));
    }

    @Test
    @DisplayName("Secure endpoint returns principal info when authenticated")
    void secureMeWithSamlAuth() throws Exception {
        // Build a minimal SAML2 Authentication for testing
        DefaultSaml2AuthenticatedPrincipal principal = new DefaultSaml2AuthenticatedPrincipal(
            "user@example.com",
            Map.of(
                "given_name", List.of("Test"),
                "family_name", List.of("User")
            )
        );
        Saml2Authentication samlAuth = new Saml2Authentication(
            principal,
            "<SAMLResponse>...</SAMLResponse>",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/secure/me").principal(samlAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.name").value("user@example.com"))
            .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("Secure claims endpoint returns SAML attributes for SAML authentication")
    void secureClaimsWithSamlAuth() throws Exception {
        DefaultSaml2AuthenticatedPrincipal principal = new DefaultSaml2AuthenticatedPrincipal(
            "user@example.com",
            Map.of(
                "email", List.of("user@example.com"),
                "groups", List.of("dev", "ops")
            )
        );
        Saml2Authentication samlAuth = new Saml2Authentication(
            principal,
            "<SAMLResponse>...</SAMLResponse>",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/secure/claims").principal(samlAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("user@example.com"))
            .andExpect(jsonPath("$.attributes.email[0]").value("user@example.com"))
            .andExpect(jsonPath("$.attributes.groups[0]").value("dev"))
            .andExpect(jsonPath("$.attributes.groups[1]").value("ops"));
    }

    @Test
    @DisplayName("Secure endpoint rejects anonymous access")
    void secureRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/secure/me"))
            .andExpect(status().is3xxRedirection()); // Redirect to SAML login when not authenticated
    }
}
