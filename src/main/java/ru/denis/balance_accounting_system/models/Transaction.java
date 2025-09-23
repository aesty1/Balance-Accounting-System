package ru.denis.balance_accounting_system.models;

import ru.denis.balance_accounting_system.dto.OperationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type")
    private OperationType operationType;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id", unique = true)
    private String referenceId;

    @Column(name = "operation_date")
    private LocalDateTime operationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Transaction() {
        this.operationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
