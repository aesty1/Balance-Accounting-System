package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveFundSummaryDTO {

    private Long accountId;
    private BigDecimal totalAmountCalculated;
    private long operationCount;
    private List<ReserveFundResponse> reserves;
}
