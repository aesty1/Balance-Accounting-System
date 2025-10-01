package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.models.ArchiveTransaction;
import ru.denis.balance_accounting_system.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArchiveTransactionRepository extends JpaRepository<ArchiveTransaction, Long> {

    @Query(value = "SELECT COALESCE(SUM(a.amount), 0) FROM archive_transactions a WHERE a.account_id = :accountId AND a.operation_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    BigDecimal sumAmountByAccountAndPeriod(@Param("accountId") Long accountId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COUNT(*) FROM archive_transactions a WHERE a.account_id = :accountId AND a.operation_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    long countByAccountIdAndPeriodDateBetween(@Param("accountId") Long accountId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM ArchiveTransaction a WHERE a.account.id = :accountId AND a.operationDate BETWEEN :startDate AND :endDate")
    List<ArchiveTransaction> findByAccountIdAndPeriod(@Param("accountId") Long accountId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}
