package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String operationType;
    private String description;
    private LocalDateTime operationDate;
    private BigDecimal newBalance;



}
