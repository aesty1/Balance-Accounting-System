package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long accountId;
    private BigDecimal amount;
    private OperationType operationType;
    private String referenceId;
    private String messageId;

    public TransactionMessage() {
    }
}
