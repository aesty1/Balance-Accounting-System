package ru.denis.balance_accounting_system.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "http://keycloak:8080/realms/balance-realm/protocol/openid-connect/certs";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();


        jwtDecoder.setJwtValidator(jwt -> {
            String issuer = jwt.getIssuer().toString();
            boolean isValid = issuer.equals("http://localhost:8081/realms/balance-realm") ||
                    issuer.equals("http://keycloak:8080/realms/balance-realm");

            if (!isValid) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_issuer", "Invalid issuer: " + issuer, null)
                );
            }

            return OAuth2TokenValidatorResult.success();
        });

        return jwtDecoder;
    }
}