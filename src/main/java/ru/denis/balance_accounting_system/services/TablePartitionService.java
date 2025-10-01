package ru.denis.balance_accounting_system.services;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.ArchiveStatus;
import ru.denis.balance_accounting_system.models.ArchiveMetadata;
import ru.denis.balance_accounting_system.repositories.ArchiveMetadataRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
public class TablePartitionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ArchiveMetadataRepository archiveMetadataRepository;

    public TablePartitionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCurrentMonthTable() {
        return "transactions_" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    public String getTableForDate(LocalDate date) {
        return "transactions_" + YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    @PostConstruct
    public void init() {
        createCurrentMonthTable();
        createNextMonthTable();
    }

    @Transactional
    public void createCurrentMonthTable() {
        String tableName = getCurrentMonthTable();
        createTableIfNotExists(tableName);
    }

    @Transactional
    public void createNextMonthTable() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        String tableName = "transactions_" + nextMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
        createTableIfNotExists(tableName);
    }

    private void createTableIfNotExists(String tableName) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " LIKE transactions";
            jdbcTemplate.execute(sql);

            // Добавляем в метаданные если еще нет
            if (!archiveMetadataRepository.existsByTableName(tableName)) {
                YearMonth yearMonth = YearMonth.parse(tableName.replace("transactions_", ""),
                        DateTimeFormatter.ofPattern("yyyyMM"));

                ArchiveMetadata metadata = new ArchiveMetadata();
                metadata.setTableName(tableName);
                metadata.setPeriodStart(yearMonth.atDay(1));
                metadata.setPeriodEnd(yearMonth.atEndOfMonth());
                metadata.setArchiveStatus(ArchiveStatus.ACTIVE);
                archiveMetadataRepository.save(metadata);
            }
        } catch (Exception e) {
            System.err.println("Error creating table " + tableName + ": " + e.getMessage());
        }
    }
}
