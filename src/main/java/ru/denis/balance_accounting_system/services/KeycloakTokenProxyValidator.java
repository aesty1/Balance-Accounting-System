package ru.denis.balance_accounting_system.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
public class KeycloakTokenProxyValidator {

    private final RestTemplate restTemplate;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    public KeycloakTokenProxyValidator() {
        this.restTemplate = new RestTemplate();
    }

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> stringResponseEntity = restTemplate.exchange(
                    keycloakUrl + "/realms/balance-realm/protocol/openid-connect/userinfo",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return stringResponseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Token validation failed:" + e.getMessage());

            return false;
        }
    }

    public Map<String, Object> getTokenInfo(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    keycloakUrl + "/realms/balance-realm/protocol/openid-connect/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return responseEntity.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get token info ", e);
        }
    }
}
