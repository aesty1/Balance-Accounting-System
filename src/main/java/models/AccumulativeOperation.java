package models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accumulative_operations")
@Data
@AllArgsConstructor
public class AccumulativeOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "description")
    private String description;

    @Column(name = "period_date")
    private LocalDate periodDate;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    public AccumulativeOperation() {
        createdAt = LocalDateTime.now();
    }
}
