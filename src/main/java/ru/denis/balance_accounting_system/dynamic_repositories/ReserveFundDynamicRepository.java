package ru.denis.balance_accounting_system.dynamic_repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.models.ReserveFund;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.ReserveFundRepository;
import ru.denis.balance_accounting_system.services.TablePartitionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReserveFundDynamicRepository {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ReserveFundRepository reserveFundRepository;

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
                description VARCHAR(500),
                reference_id VARCHAR(100) UNIQUE COMMENT 'Идентификатор для идемпотентности',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """, tableName);

        jdbcTemplate.execute(sql);
    }

    public ReserveFund save(ReserveFund reserveFund) {
        String tableName = getTableName(reserveFund.getCreatedAt().toLocalDate());
        createMonthlyTable(reserveFund.getCreatedAt().toLocalDate());

        String sql = String.format("""
            INSERT INTO %s (account_id, amount, description, reference_id, created_at) 
            VALUES (?, ?, ?, ?, ?)
            """, tableName);

        jdbcTemplate.update(sql,
                reserveFund.getAccountId().getId(),
                reserveFund.getAmount(),
                reserveFund.getDescription(),
                reserveFund.getReferenceId(),
                reserveFund.getCreatedAt());

        return reserveFund;
    }

    public List<ReserveFund> findByMonth(LocalDate date) {
        String tableName = getTableName(date);
        createMonthlyTable(date);

        String sql = String.format("SELECT * FROM %s", tableName);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReserveFund reserveFund = new ReserveFund();
            reserveFund.setId(rs.getLong("id"));
            reserveFund.setAccountId(accountRepository.findByIdWithLock(rs.getLong("account_id")).orElseThrow(() ->
                    new EntityNotFoundException("Account not found")));
            reserveFund.setAmount(rs.getBigDecimal("amount"));
            reserveFund.setDescription(rs.getString("description"));
            reserveFund.setReferenceId(rs.getString("reference_id"));
            reserveFund.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
            return reserveFund;
        });
    }

    public List<ReserveFund> findByPeriod(LocalDate startDate, LocalDate endDate) {
        List<String> tableQueries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        LocalDate currentMonth = startDate.withDayOfMonth(1);
        LocalDate endMonth = endDate.withDayOfMonth(1);

        while(!currentMonth.isAfter(endMonth)) {
            String tableName = getTableName(currentMonth);
            createMonthlyTable(currentMonth);

            LocalDateTime monthStart = currentMonth.atStartOfDay();
            LocalDateTime monthEnd = currentMonth.plusMonths(1).atStartOfDay().minusNanos(1);

            LocalDateTime filterStart = currentMonth.equals(startDate.withDayOfMonth(1)) ? startDate.atStartOfDay() : monthStart;
            LocalDateTime filterEnd = currentMonth.equals(endDate.withDayOfMonth(1)) ? endDate.atTime(23, 59, 59, 99999999) : monthEnd;

            tableQueries.add(String.format(
                    "SELECT * FROM %s WHERE created_at >= ? AND created_at <= ?",
                    tableName));

            params.add(filterStart);
            params.add(filterEnd);

            currentMonth = currentMonth.plusMonths(1);
        }

        if(tableQueries.isEmpty()) {
            return new ArrayList<>();
        }

        String unionSql = String.join(" UNION ALL ", tableQueries) + " ORDER BY created_at";

        return jdbcTemplate.query(unionSql, params.toArray(), (rs, rowNum) -> {
            ReserveFund reserveFund = new ReserveFund();
            reserveFund.setId(rs.getLong("id"));
            reserveFund.setAccountId(accountRepository.findByIdWithLock(rs.getLong("account_id")).orElseThrow(() ->
                    new EntityNotFoundException("Account not found")));
            reserveFund.setAmount(rs.getBigDecimal("amount"));
            reserveFund.setDescription(rs.getString("description"));
            reserveFund.setReferenceId(rs.getString("reference_id"));
            reserveFund.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return reserveFund;
        });
    }

    private String getTableName(LocalDate date) {
        return "reserve_fund_" + date.getYear()  +
                String.format("%02d", date.getMonthValue());
    }
}
