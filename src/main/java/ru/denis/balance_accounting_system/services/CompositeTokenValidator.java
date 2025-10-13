package ru.denis.balance_accounting_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.TokenInfo;

import java.util.List;
import java.util.Map;

@Service
public class CompositeTokenValidator {

    @Autowired
    private KeycloakTokenProxyValidator proxyValidator;

    @Autowired
    private LocalTokenValidator localTokenValidator;

    public boolean validateToken(String token, boolean useProxy) {
        if(useProxy) {
            return proxyValidator.validateToken(token);
        } else {
            return localTokenValidator.validateToken(token);
        }
    }

    public TokenInfo getTokenInfo(String token, boolean useProxy) {
        if(useProxy) {
            Map<String, Object> userInfo = proxyValidator.getTokenInfo(token);

            return new TokenInfo(
                    (String) userInfo.get("sub"),
                    (String) userInfo.get("preferred_username"),
                    (String) userInfo.get("email"),
                    (List<String>) userInfo.get("roles")
            );
        } else {
            Jwt jwt = localTokenValidator.parseToken(token);

            return new TokenInfo(
                    jwt.getSubject(),
                    jwt.getClaimAsString("preferred_username"),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsStringList("roles")
            );
        }
    }
}
