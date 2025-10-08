package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.denis.balance_accounting_system.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROm Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);


}
