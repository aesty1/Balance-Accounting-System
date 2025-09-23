package ru.denis.balance_accounting_system.repositories;

import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {

    Optional<ProcessedMessage> findByMessageId(String messageId);
    
    boolean existsByMessageId(String messageId);
}
