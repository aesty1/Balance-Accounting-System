package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ReserveFund;
import ru.denis.balance_accounting_system.models.Transaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReserveFundRepository extends JpaRepository<ReserveFund, Long> {
    List<ReserveFund> findAllByAccountId(Account accountId);

    Optional<ReserveFund> findByAccountIdAndReferenceId(Account accountId, String referenceId);
}
