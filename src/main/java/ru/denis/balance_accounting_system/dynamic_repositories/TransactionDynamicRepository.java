package ru.denis.balance_accounting_system.dynamic_repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.services.TablePartitionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class TransactionDynamicRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TablePartitionService tablePartitionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 1 1 * ?")
    public void createMothTableTimer() {
        LocalDate nextMonth = LocalDate.parse(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy_MM")));

        createMonthlyTable(nextMonth);
    }

    public void createMonthlyTable(LocalDate date) {
        String tableName = getTableName(date);
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                account_id BIGINT NOT NULL,
                amount DECIMAL(15,2) NOT NULL COMMENT 'Сумма операции с точностью 2 знака',
                operation_type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT 'Тип операции: приход/расход',
                description VARCHAR(500),
                reference_id VARCHAR(100) UNIQUE COMMENT 'Идентификатор для идемпотентности',
                operation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_account_id (account_id),
                INDEX idx_operation_date (operation_date),
                INDEX idx_reference_id (reference_id),
                FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """, tableName);

        jdbcTemplate.execute(sql);
    }

    public Transaction save(Transaction transaction) {
        String tableName = getTableName(transaction.getCreatedAt().toLocalDate());
        System.out.println(transaction.getCreatedAt().toLocalDate());
        createMonthlyTable(transaction.getCreatedAt().toLocalDate());

        String sql = String.format("""
            INSERT INTO %s (account_id, amount, operation_type, description, reference_id, operation_date, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, tableName);

        jdbcTemplate.update(sql,
                transaction.getAccount().getId(),
                transaction.getAmount(),
                transaction.getOperationType().name(),
                transaction.getDescription(),
                transaction.getReferenceId(),
                transaction.getOperationDate(),
                transaction.getCreatedAt());

        return transaction;
    }

    public List<Transaction> findByMonth(LocalDate date) {
        String tableName = getTableName(date);
        createMonthlyTable(date);

        String sql = String.format("SELECT * FROM %s", tableName);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getLong("id"));
            transaction.setAccount(accountRepository.findByIdWithLock(rs.getLong("account_id")).orElseThrow(() ->
                    new EntityNotFoundException("Account not found")));
            transaction.setAmount(rs.getBigDecimal("amount"));
            transaction.setOperationType((OperationType) rs.getObject("operation_type"));
            transaction.setDescription(rs.getString("description"));
            transaction.setReferenceId(rs.getString("reference_id"));
            transaction.setOperationDate((LocalDateTime) rs.getObject("operation_date"));
            transaction.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
            return transaction;
        });
    }

    private String getTableName(LocalDate date) {
        return "transactions_" + date.getYear()  +
                String.format("%02d", date.getMonthValue());
    }
}
