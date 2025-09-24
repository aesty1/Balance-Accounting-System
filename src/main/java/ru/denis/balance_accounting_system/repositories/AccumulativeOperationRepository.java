package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccumulativeOperationRepository extends JpaRepository<AccumulativeOperation, Long> {

    Optional<AccumulativeOperation> findByReferenceId(String referenceId);

    @Query("SELECT COUNT(a) > 0 FROM AccumulativeOperation a WHERE a.referenceId = :referenceId")
    boolean existsByReferenceId(@Param("referenceId") String referenceId);

    List<AccumulativeOperation> findByAccountIdAndPeriodDate(Long accountId, LocalDate periodDate);

    // Сумма накопительных списаний за период с высокой точностью
    @Query("SELECT SUM(a.amount) FROM AccumulativeOperation a WHERE a.account.id = :accountId AND a.periodDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumAmountByAccountAndPeriod(@Param("accountId") Long accountId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // Количество операций за период
    @Query("SELECT COUNT(a) FROM AccumulativeOperation a WHERE a.account.id = :accountId AND a.periodDate BETWEEN :startDate AND :endDate")
    long countByAccountIdAndPeriodDateBetween(@Param("accountId") Long accountId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
