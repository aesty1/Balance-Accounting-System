package ru.denis.balance_accounting_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class LocalTokenValidator {

    private final JwtDecoder jwtDecoder;

    @Value("${keycloak.url:http://keycloak:8080}")
    private String keycloakUrl;

    @Value("${keycloak.local.url:http://localhost:8081}")
    private String keycloakLocalUrl;

    // Используем @Autowired вместо конструктора
    @Autowired
    public LocalTokenValidator(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            return isTokenValid(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    public Jwt parseToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token: " + e.getMessage(), e);
        }
    }

    public String getUsername(String token) {
        Jwt jwt = parseToken(token);
        return jwt.getClaimAsString("preferred_username");
    }

    public List<String> getRoles(String token) {
        Jwt jwt = parseToken(token);

        // Правильное извлечение ролей из Keycloak токена
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                return roles;
            }
        }

        return List.of();
    }

    private boolean isTokenValid(Jwt jwt) {
        // Проверка срока действия
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            return false;
        }

        // Проверка issuer
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        String expectedIssuer = keycloakLocalUrl + "/realms/balance-realm";

        boolean issuerValid = issuer.equals(expectedIssuer);
        if (!issuerValid) {
            System.err.println("Invalid issuer");
        }

        return issuerValid;
    }
}