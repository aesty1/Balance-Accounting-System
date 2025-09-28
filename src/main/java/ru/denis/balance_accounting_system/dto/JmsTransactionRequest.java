package ru.denis.balance_accounting_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JmsTransactionRequest {

    private BigDecimal amount;
    private String referenceId;
}
