package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AccumulativeResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amountCalculated;
    private BigDecimal amountDisplay;
    private String description;
    private LocalDate periodDate;
    private BigDecimal newBalance;

    public AccumulativeResponse(Long id, Long accountId, BigDecimal amountCalculated,
                                String description, LocalDate periodDate, BigDecimal newBalance) {
        this.id = id;
        this.accountId = accountId;
        this.amountCalculated = amountCalculated;
        this.amountDisplay = amountCalculated.setScale(2, RoundingMode.HALF_UP);
        this.description = description;
        this.periodDate = periodDate;
        this.newBalance = newBalance;
    }
}
