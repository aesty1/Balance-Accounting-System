package ru.denis.balance_accounting_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserve_fund")
@Data
@AllArgsConstructor
public class ReserveFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account accountId;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id", unique = true)
    private String referenceId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ReserveFund() {
        this.createdAt = LocalDateTime.now();
    }
}
