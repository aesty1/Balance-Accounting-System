package ru.denis.balance_accounting_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.denis.balance_accounting_system.dto.ArchiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive_metadata")
@Data
@AllArgsConstructor
public class ArchiveMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", unique = true, nullable = false)
    private String tableName;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "record_count")
    private Long recordCount;

    @Column(name = "total_size_mb")
    private Long totalSizeMb;

    @Column(name = "archive_status")
    @Enumerated(EnumType.STRING)
    private ArchiveStatus archiveStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    public ArchiveMetadata() {
        this.createdAt = LocalDateTime.now();
        this.archiveStatus = ArchiveStatus.ACTIVE;
    }
}
