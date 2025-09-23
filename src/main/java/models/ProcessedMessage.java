package models;

import dto.ProcessStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_messages")
@Data
@AllArgsConstructor
public class ProcessedMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcessStatus status;

    public ProcessedMessage() {
        processedAt = LocalDateTime.now();
    }
}
