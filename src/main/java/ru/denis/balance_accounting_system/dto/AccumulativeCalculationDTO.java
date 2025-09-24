package ru.denis.balance_accounting_system.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccumulativeCalculationDTO {
    private int period;
    private BigDecimal amountCalculated;    // Точное значение (5 знаков)
    private BigDecimal amountDisplay;       // Округленное значение (2 знака)
    private BigDecimal interest;            // Начисленные проценты (5 знаков)

    // Геттеры и сеттеры
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public BigDecimal getAmountCalculated() { return amountCalculated; }
    public void setAmountCalculated(BigDecimal amountCalculated) {
        this.amountCalculated = amountCalculated;
        this.amountDisplay = amountCalculated.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmountDisplay() { return amountDisplay; }
    public void setAmountDisplay(BigDecimal amountDisplay) { this.amountDisplay = amountDisplay; }

    public BigDecimal getInterest() { return interest; }
    public void setInterest(BigDecimal interest) { this.interest = interest; }
}
