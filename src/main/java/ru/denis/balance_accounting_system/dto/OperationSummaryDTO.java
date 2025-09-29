package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.denis.balance_accounting_system.models.Transaction;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationSummaryDTO {

    private Long accountId;
    private String period;
    private BigDecimal totalAmountCalculated;
    private long operationCount;
    private List<TransactionResponse> transactions;
}
