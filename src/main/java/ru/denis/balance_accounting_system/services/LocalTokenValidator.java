package ru.denis.balance_accounting_system.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LocalTokenValidator {

    private final JwtDecoder jwtDecoder;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    public LocalTokenValidator() {
        this.jwtDecoder = createJwtDecoder();
    }

    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            return isTokenValid(jwt);
        } catch (Exception e) {
            System.err.println("Local token validation failed: " + e.getMessage());

            return false;
        }
    }

    public Jwt parseToken(String token) {
        return jwtDecoder.decode(token);
    }

    public String getUsername(String token) {
        Jwt jwt = parseToken(token);

        return jwt.getClaimAsString("preferred_username");
    }

    public List<String> getRoles(String token) {
        Jwt jwt = parseToken(token);

        return jwt.getClaimAsStringList("roles");
    }

    private JwtDecoder createJwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(keycloakUrl + "/realms/balance-realm/protocol/openid-connect/certs").build();
    }

    private boolean isTokenValid(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            return false;
        }

        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        String expectedIssuer = keycloakUrl + "/realms/balance-realm";

        return issuer.equals(expectedIssuer);
    }
}
