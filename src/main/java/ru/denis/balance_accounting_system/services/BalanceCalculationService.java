package ru.denis.balance_accounting_system.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ArchiveMetadata;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.ArchiveMetadataRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BalanceCalculationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ArchiveMetadataRepository archiveMetadataRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TablePartitionService tablePartitionService;

    public BigDecimal calculateCurrentBalance(Long accountId) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() ->
                new EntityNotFoundException("Account npt found"));

        return account.getBalance();
    }

    public BigDecimal calculateBalanceForDate(Long accountId, LocalDate localDate) {
        BigDecimal balance = BigDecimal.ZERO;

        List<ArchiveMetadata> relevantArchives  = archiveMetadataRepository.findByPeriodStartLessThanEqual(LocalDate.from(localDate.atStartOfDay()));

        for(ArchiveMetadata metadata : relevantArchives) {
            BigDecimal periodBalance = calculateBalanceFromTable(accountId, metadata.getTableName(), localDate);
            balance = balance.add(periodBalance);
        }

        BigDecimal currentPeriodBalance = calculateBalanceFromCurrentTable(accountId, localDate);

        balance.add(currentPeriodBalance);

        return balance;
    }

    public BigDecimal calculateBalanceFromCurrentTable(Long accountId, LocalDate untilDate) {
        String currentTable = tablePartitionService.getCurrentMonthTable();

        return calculateBalanceFromTable(accountId, currentTable, untilDate);
    }



    private BigDecimal calculateBalanceFromTable(Long accountId, String tableName, LocalDate untilDate) {
        try {
            String sql = "SELECT COALESCE(SUM(CASE WHEN operation_type = 'INCOME' THEN amount ELSE -amount END), 0) " +
                    "FROM " + tableName + " WHERE account_id = ? AND DATE(operation_date) <= ?";

            BigDecimal result = jdbcTemplate.queryForObject(sql, new Object[]{accountId, untilDate}, BigDecimal.class);
            return result != null ? result : BigDecimal.ZERO;
        } catch (BadSqlGrammarException e) {
            // Если таблица не существует, возвращаем 0
            System.err.println("Table " + tableName + " does not exist, returning 0");
            return BigDecimal.ZERO;
        }
    }
}
