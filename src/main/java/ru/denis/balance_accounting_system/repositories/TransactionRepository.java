package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.referenceId = :referenceId")
    boolean existsByReferenceId(@Param("referenceId") String referenceId);

    void deleteByReferenceId(String referenceId);

    @Query(value = "SELECT COUNT(*) FROM transactions a WHERE a.account_id = :accountId AND a.operation_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    long countByAccountIdAndPeriodDateBetween(@Param("accountId") Long accountId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COALESCE(SUM(a.amount), 0) FROM transactions a WHERE a.account_id = :accountId AND a.operation_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    BigDecimal sumAmountByAccountAndPeriod(@Param("accountId") Long accountId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Transaction a WHERE a.account.id = :accountId AND a.operationDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByAccountIdAndPeriod(@Param("accountId") Long accountId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    List<Transaction> findAllByOperationDateBefore(LocalDateTime operationDateBefore);
}
