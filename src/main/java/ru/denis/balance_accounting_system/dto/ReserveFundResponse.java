package ru.denis.balance_accounting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.denis.balance_accounting_system.models.ReserveFund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveFundResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String description;
    private String referenceId;
    private LocalDateTime createdAt;

}
