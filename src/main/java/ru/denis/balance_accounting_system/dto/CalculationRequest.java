package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculationRequest {

    private BigDecimal baseAmount;
    private BigDecimal rate;
    private int periods;
    private BigDecimal additionalFee;
}
