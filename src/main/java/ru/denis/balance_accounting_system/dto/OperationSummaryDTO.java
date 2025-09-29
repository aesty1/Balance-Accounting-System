package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationSummaryDTO {

    private Long accountId;
    private String period;
    private BigDecimal totalAmountCalculated;
    private long operationCount;
}
