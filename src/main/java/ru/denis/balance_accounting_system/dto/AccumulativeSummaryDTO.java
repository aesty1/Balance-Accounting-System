package ru.denis.balance_accounting_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AccumulativeSummaryDTO {
    private Long accountId;
    private String period;
    private BigDecimal totalAmountCalculated;
    private BigDecimal totalAmountDisplay;
    private long operationCount;

    public AccumulativeSummaryDTO(Long accountId, String period,
                                  BigDecimal totalAmountCalculated,
                                  BigDecimal totalAmountDisplay,
                                  long operationCount) {
        this.accountId = accountId;
        this.period = period;
        this.totalAmountCalculated = totalAmountCalculated;
        this.totalAmountDisplay = totalAmountDisplay;
        this.operationCount = operationCount;
    }


    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getTotalAmountCalculated() { return totalAmountCalculated; }
    public void setTotalAmountCalculated(BigDecimal totalAmountCalculated) {
        this.totalAmountCalculated = totalAmountCalculated;
    }

    public BigDecimal getTotalAmountDisplay() { return totalAmountDisplay; }
    public void setTotalAmountDisplay(BigDecimal totalAmountDisplay) {
        this.totalAmountDisplay = totalAmountDisplay;
    }

    public long getOperationCount() { return operationCount; }
    public void setOperationCount(long operationCount) { this.operationCount = operationCount; }
}
