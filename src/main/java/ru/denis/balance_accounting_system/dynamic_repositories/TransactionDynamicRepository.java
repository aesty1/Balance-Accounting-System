package ru.denis.balance_accounting_system.dynamic_repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.services.TablePartitionService;

import java.time.LocalDate;
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

    public Transaction save(Transaction transaction) {
        String tableName = tablePartitionService.getTableForDate(LocalDate.from(transaction.getOperationDate()));

        String sql = "INSERT INTO " + tableName + " (account_id, amount, operation_type, description, reference_id, operation_date, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

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

    public List<Transaction> findByAccountIdAndPeriod(Long accountId, YearMonth period) {
        String tableName = "transactions_" + period.format(DateTimeFormatter.ofPattern("yyyyMM"));

        String sql = "SELECT * FROM " + tableName + " WHERE account_id = ? ORDER BY operation_date";

        return jdbcTemplate.query(sql, new Object[]{accountId}, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getLong("id"));
            transaction.setAccount(accountRepository.findByIdWithLock(rs.getLong("account_id")).orElseThrow(() ->
                    new EntityNotFoundException("Account not found")));
            transaction.setAmount(rs.getBigDecimal("amount"));
            transaction.setOperationType(OperationType.valueOf(rs.getString("operation_type")));
            transaction.setDescription(rs.getString("description"));
            transaction.setReferenceId(rs.getString("reference_id"));
            transaction.setOperationDate(rs.getTimestamp("operation_date").toLocalDateTime());
            transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return transaction;
        });
    }
}
