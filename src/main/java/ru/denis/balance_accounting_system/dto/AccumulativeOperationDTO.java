package ru.denis.balance_accounting_system.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class AccumulativeOperationDTO {

    private Long id;
    private Long accountId;
    private BigDecimal amountCalculated;
    private BigDecimal amountDisplay;
    private String description;
    private LocalDate periodDate;
    private String referenceId;
    private LocalDateTime createdAt;


    public AccumulativeOperationDTO(AccumulativeOperation operation) {
        this.id = operation.getId();
        this.accountId = operation.getAccount().getId();
        this.amountCalculated = operation.getAmount();
        this.amountDisplay = operation.getAmount() != null ?
                operation.getAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.description = operation.getDescription();
        this.periodDate = operation.getPeriodDate();
        this.referenceId = operation.getReferenceId();
        this.createdAt = operation.getCreatedAt();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getAmountCalculated() { return amountCalculated; }
    public void setAmountCalculated(BigDecimal amountCalculated) {
        this.amountCalculated = amountCalculated;
        this.amountDisplay = amountCalculated != null ?
                amountCalculated.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getAmountDisplay() { return amountDisplay; }
    public void setAmountDisplay(BigDecimal amountDisplay) { this.amountDisplay = amountDisplay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getPeriodDate() { return periodDate; }
    public void setPeriodDate(LocalDate periodDate) { this.periodDate = periodDate; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }



}
