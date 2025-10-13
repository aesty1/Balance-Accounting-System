package ru.denis.balance_accounting_system.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.dto.TokenInfo;
import ru.denis.balance_accounting_system.services.CompositeTokenValidator;
import ru.denis.balance_accounting_system.services.KeycloakTokenProxyValidator;
import ru.denis.balance_accounting_system.services.LocalTokenValidator;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CompositeTokenValidator tokenValidator;

    @Autowired
    private LocalTokenValidator localTokenValidator;

    @Autowired
    private KeycloakTokenProxyValidator proxyValidator;

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Its ok");
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader, @RequestParam(defaultValue = "false") boolean useProxy) {

        String token = extractToken(authHeader);

        boolean isValid = tokenValidator.validateToken(token, useProxy);
        TokenInfo tokenInfo = null;

        if(isValid) {
            tokenInfo = tokenValidator.getTokenInfo(token, useProxy);
        }

        Map<String, Object> response = new HashMap<>();

        response.put("valid", isValid);
        response.put("validation_method", useProxy ? "proxy" : "local");
        if(tokenInfo != null) {
            response.put("user_info", tokenInfo);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/debug/token")
    public ResponseEntity<Map<String, Object>> debugToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null) {
            response.put("error", "No Authorization header");
            return ResponseEntity.badRequest().body(response);
        }

        String token = extractToken(authHeader);
        response.put("token_length", token.length());
        response.put("token_prefix", token.substring(0, Math.min(20, token.length())) + "...");

        try {
            // Пробуем локальную валидацию
            boolean localValid = localTokenValidator.validateToken(token);
            response.put("local_validation", localValid);

            if (localValid) {
                Jwt jwt = localTokenValidator.parseToken(token);
                response.put("subject", jwt.getSubject());
                response.put("issuer", jwt.getIssuer());
                response.put("expires_at", jwt.getExpiresAt());
            }
        } catch (Exception e) {
            response.put("local_validation_error", e.getMessage());
        }

        try {
            // Пробуем прокси валидацию
            boolean proxyValid = proxyValidator.validateToken(token);
            response.put("proxy_validation", proxyValid);
        } catch (Exception e) {
            response.put("proxy_validation_error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    private String extractToken(String authHeader) {
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        throw new IllegalArgumentException("Invalid Authorization Header");
    }
}
