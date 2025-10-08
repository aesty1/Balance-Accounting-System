package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnifiedTransactionMessage {

    private OperationType operationType;
    private Long accountId;
    private BigDecimal amount;
    private String referenceId;
    private String messageId;

    // Поля только для ACCUMULATIVE
    private String description;
    private String period;
}
