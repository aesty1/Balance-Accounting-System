package ru.denis.balance_accounting_system.services;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ArchiveMetadata;
import ru.denis.balance_accounting_system.models.ArchiveTransaction;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.ArchiveMetadataRepository;
import ru.denis.balance_accounting_system.repositories.ArchiveTransactionRepository;
import ru.denis.balance_accounting_system.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Slf4j
public class ArchiveService {

    @Autowired
    private ArchiveTransactionRepository archiveTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    @Scheduled(cron = "0 0 1 1 * ?")
    public void archiveOldData() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        List<Transaction> oldTransactions = transactionRepository.findAllByOperationDateBefore(oneYearAgo);

        if(oldTransactions.isEmpty()) {
            return;
        }

        try {
            List<ArchiveTransaction> archiveTransactions = oldTransactions.stream().map(this::createArchiveTransaction).toList();

            archiveTransactionRepository.saveAll(archiveTransactions);
            transactionRepository.deleteAll(oldTransactions);
        } catch (Exception e) {
            throw e;
        }

    }

    private ArchiveTransaction createArchiveTransaction(Transaction transaction) {
        ArchiveTransaction archiveTransaction = new ArchiveTransaction();

        archiveTransaction.setAccount(transaction.getAccount());
        archiveTransaction.setAmount(transaction.getAmount());
        archiveTransaction.setOperationType(transaction.getOperationType());
        archiveTransaction.setOperationDate(transaction.getOperationDate());
        archiveTransaction.setDescription(transaction.getDescription());
        archiveTransaction.setReferenceId(transaction.getReferenceId());

        return archiveTransaction;
    }
}
