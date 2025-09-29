package ru.denis.balance_accounting_system.broker_services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
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

    @JmsListener(destination = "transaction.queue")
    @Transactional
    public void processTransaction(@Payload TransactionMessage message) {
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
    }

    @JmsListener(destination = "accumulative_queue")
    @Transactional
    public void processAccumulative(@Payload AccumulativeRequest request) {
        if(request.getReferenceId() != null && accumulativeOperationRepository.existsByReferenceId(request.getReferenceId())) {
            throw new IllegalArgumentException("Accumulative operation already exists");
        }

        Account account = accountRepository.findByIdWithLock(request.getAccount_id()).orElseThrow(() ->
                new EntityNotFoundException("Account not found"));

        BigDecimal amountCheck = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if(account.getBalance().compareTo(amountCheck) < 0) {
            throw new IllegalArgumentException("Insufficient funds for accumulative operation.");
        }

        LocalDate periodDate = parsePeriodDate(request.getPeriod());

        AccumulativeOperation operation = new AccumulativeOperation();
        operation.setAccount(account);
        operation.setDescription(request.getDescription());
        operation.setPeriodDate(periodDate);
        operation.setReferenceId(request.getReferenceId());
        operation.setAmount(request.getAmount());

        AccumulativeOperation savedOperation = accumulativeOperationRepository.save(operation);

        BigDecimal amountToDeduct = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        account.setBalance(account.getBalance().subtract(amountToDeduct));
        account.setVersion(account.getVersion() + 1);

        Account updatedAccount = accountRepository.save(account);
    }

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

    private void saveProcessedMessage(TransactionMessage message, String status) {
        ProcessedMessage processedMessage = new ProcessedMessage();

        processedMessage.setAccountId(message.getAccountId());
        processedMessage.setMessageId(message.getMessageId());
        processedMessage.setMessageType("TRANSACTION");
        processedMessage.setStatus(ProcessStatus.valueOf(status));

        processedMessageRepository.save(processedMessage);
    }
}
