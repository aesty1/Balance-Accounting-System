package ru.denis.balance_accounting_system.broker_services;

import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.denis.balance_accounting_system.configs.BalanceMetrics;
import ru.denis.balance_accounting_system.dto.*;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;
import ru.denis.balance_accounting_system.models.ProcessedMessage;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.AccumulativeOperationRepository;
import ru.denis.balance_accounting_system.repositories.ProcessedMessageRepository;
import ru.denis.balance_accounting_system.services.BalanceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class MessageReceiver {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private AccumulativeOperationRepository accumulativeOperationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceMetrics balanceMetrics;

    @JmsListener(destination = "transaction.queue")
    @Transactional
    public void processTransaction(@Payload UnifiedTransactionMessage message) {
        if(message.getOperationType() != OperationType.ACCUMULATIVE) {
            // Проверка на идемпотентность
            if(processedMessageRepository.existsByMessageId(message.getMessageId())) {
                return;
            }

            try {
                TransactionRequest request = new TransactionRequest();

                request.setAmount(message.getAmount());
                request.setReferenceId(message.getReferenceId());
                request.setDescription("JMS transaction: " + message.getOperationType());

                if(message.getOperationType() == OperationType.INCOME) {
                    balanceService.addIncome(message.getAccountId(), request);
                } else if(message.getOperationType() == OperationType.EXPENSE) {
                    balanceService.addExpense(message.getAccountId(), request);
                }

                saveProcessedMessage(message, "PROCESSED");
            } catch (Exception e) {
                saveProcessedMessage(message, "ERROR");
                throw new RuntimeException("Error processing JMS message", e);
            }
        } else {
            Timer.Sample sample = balanceMetrics.startAccumulativeTimer();
            try {
                if(message.getReferenceId() != null && accumulativeOperationRepository.existsByReferenceId(message.getReferenceId())) {
                    throw new IllegalArgumentException("Accumulative operation already exists");
                }

                Account account = accountRepository.findByIdWithLock(message.getAccountId()).orElseThrow(() ->
                        new EntityNotFoundException("Account not found"));

                BigDecimal amountCheck = message.getAmount().setScale(2, RoundingMode.HALF_UP);
                if(account.getBalance().compareTo(amountCheck) < 0) {
                    throw new IllegalArgumentException("Insufficient funds for accumulative operation.");
                }

                LocalDate periodDate = parsePeriodDate(message.getPeriod());

                AccumulativeOperation operation = new AccumulativeOperation();
                operation.setAccount(account);
                operation.setDescription(message.getDescription());
                operation.setPeriodDate(periodDate);
                operation.setReferenceId(message.getReferenceId());
                operation.setAmount(message.getAmount());

                AccumulativeOperation savedOperation = accumulativeOperationRepository.save(operation);

                BigDecimal amountToDeduct = message.getAmount().setScale(2, RoundingMode.HALF_UP);
                account.setBalance(account.getBalance().subtract(amountToDeduct));
                account.setVersion(account.getVersion() + 1);

                accountRepository.save(account);
                balanceMetrics.incrementAccumulativeTransactions();

            } finally {
                balanceMetrics.stopAccumulativeTimer(sample);
            }
        }

    }
//
//    @JmsListener(destination = "accumulative.queue")
//    @Transactional
//    public void processAccumulative(@Payload AccumulativeRequest request) {
//        Timer.Sample sample = balanceMetrics.startAccumulativeTimer();
//        try {
//            if(request.getReferenceId() != null && accumulativeOperationRepository.existsByReferenceId(request.getReferenceId())) {
//                throw new IllegalArgumentException("Accumulative operation already exists");
//            }
//
//            Account account = accountRepository.findByIdWithLock(request.getAccount_id()).orElseThrow(() ->
//                    new EntityNotFoundException("Account not found"));
//
//            BigDecimal amountCheck = request.getAmount().setScale(2, RoundingMode.HALF_UP);
//            if(account.getBalance().compareTo(amountCheck) < 0) {
//                throw new IllegalArgumentException("Insufficient funds for accumulative operation.");
//            }
//
//            LocalDate periodDate = parsePeriodDate(request.getPeriod());
//
//            AccumulativeOperation operation = new AccumulativeOperation();
//            operation.setAccount(account);
//            operation.setDescription(request.getDescription());
//            operation.setPeriodDate(periodDate);
//            operation.setReferenceId(request.getReferenceId());
//            operation.setAmount(request.getAmount());
//
//            AccumulativeOperation savedOperation = accumulativeOperationRepository.save(operation);
//
//            BigDecimal amountToDeduct = request.getAmount().setScale(2, RoundingMode.HALF_UP);
//            account.setBalance(account.getBalance().subtract(amountToDeduct));
//            account.setVersion(account.getVersion() + 1);
//
//            accountRepository.save(account);
//            balanceMetrics.incrementAccumulativeTransactions();
//
//
//        } finally {
//            balanceMetrics.stopAccumulativeTimer(sample);
//        }
//    }

    private LocalDate parsePeriodDate(String period) {
        if(period == null || period.isEmpty()) {
            return LocalDate.now().withDayOfMonth(1);
        }

        try {
            YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));

            return yearMonth.atEndOfMonth();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid period format. Use YYYY-MM");
        }
    }

    private void saveProcessedMessage(UnifiedTransactionMessage message, String status) {
        ProcessedMessage processedMessage = new ProcessedMessage();

        processedMessage.setAccountId(message.getAccountId());
        processedMessage.setMessageId(message.getMessageId());
        processedMessage.setMessageType("TRANSACTION");
        processedMessage.setStatus(ProcessStatus.valueOf(status));

        processedMessageRepository.save(processedMessage);
    }
}
