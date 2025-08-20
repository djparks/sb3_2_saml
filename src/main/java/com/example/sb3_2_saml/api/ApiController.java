package com.example.sb3_2_saml.api;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple REST API demonstrating both public and secured endpoints.
 */
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {

    /**
     * Public endpoint. No authentication required.
     */
    @GetMapping("/public/hello")
    public Map<String, Object> hello() {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Hello from sb3_2_saml!");
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    /**
     * Secured endpoint. Shows basic info about the current user.
     */
    @GetMapping("/secure/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> body = new HashMap<>();
        body.put("authenticated", authentication != null && authentication.isAuthenticated());
        if (authentication != null) {
            body.put("name", authentication.getName());
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            body.put("authorities", authorities);
        }
        return body;
    }

    /**
     * Secured endpoint. Shows SAML attributes (if authenticated via SAML).
     */
    @GetMapping("/secure/claims")
    public Map<String, Object> claims(Authentication authentication) {
        Map<String, Object> body = new HashMap<>();
        if (authentication instanceof Saml2Authentication samlAuth) {
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) samlAuth.getPrincipal();
            Map<String, List<Object>> attributes = new HashMap<>();
            principal.getAttributes().forEach((k, v) -> attributes.put(k, List.copyOf(v)));
            body.put("name", principal.getName());
            body.put("attributes", attributes);
        } else {
            body.put("message", "Not a SAML2 authentication or not authenticated");
        }
        return body;
    }
}
