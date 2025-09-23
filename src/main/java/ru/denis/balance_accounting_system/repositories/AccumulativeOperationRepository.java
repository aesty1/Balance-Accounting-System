package ru.denis.balance_accounting_system.repositories;

import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccumulativeOperationRepository extends JpaRepository<AccumulativeOperation, Long> {
}
