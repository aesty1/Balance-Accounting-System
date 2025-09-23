package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.referenceId = :referenceId")
    boolean existsByReferenceId(@Param("referenceId") String referenceId);
}
