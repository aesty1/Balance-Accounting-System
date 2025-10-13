package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class TokenInfo {

    private final String userId;
    private final String username;
    private final String email;
    private final List<String> roles;
}
