package ru.denis.balance_accounting_system.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.*;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
public class BalanceService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse addIncome(Long account_id, TransactionRequest request) {
        return performTransaction(account_id, request, OperationType.INCOME);
    }

    @Transactional
    public TransactionResponse addExpense(Long account_id, TransactionRequest request) {
        return performTransaction(account_id, request, OperationType.EXPENSE);
    }

    @Transactional
    public void removeOperation(String referenceId) {

        if(referenceId == null || !transactionRepository.existsByReferenceId(referenceId)) {
            throw new IllegalArgumentException("Reference id not found");
        }

        transactionRepository.deleteByReferenceId(referenceId);
    }

    @Transactional
    public void editOperation(String referenceId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByReferenceId(referenceId).orElseThrow(() ->
                new EntityNotFoundException("Reference id not found"));

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setReferenceId(request.getReferenceId());

        transactionRepository.save(transaction);
    }

    private TransactionResponse performTransaction(Long accountId, TransactionRequest request, OperationType operationType) {
        if(transactionRepository.existsByReferenceId(request.getReferenceId()) && request.getReferenceId() != null) {
            throw new IllegalArgumentException("Transaction already exists");
        }

        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() ->
                new EntityNotFoundException("Account not found"));

        if (operationType == OperationType.EXPENSE && account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Current balance: " + request.getAmount());
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setOperationType(operationType);
        transaction.setDescription(request.getDescription());
        transaction.setReferenceId(request.getReferenceId());

        Transaction savedTransaction = transactionRepository.save(transaction);

        updateAccountBalance(account, request.getAmount(), operationType);

        Account updatedAccount = accountRepository.save(account);

        return new TransactionResponse(
                savedTransaction.getId(),
                accountId,
                request.getAmount(),
                operationType.name(),
                request.getDescription(),
                savedTransaction.getOperationDate(),
                updatedAccount.getBalance()
        );
    }

    // Явное обновление в бд
    private void updateAccountBalance(Account account, BigDecimal amount, OperationType operationType) {
        if(operationType == OperationType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    @Transactional
    public BigDecimal getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new EntityNotFoundException("Account not found"));

        return account.getBalance();
    }

    public OperationSummaryDTO getOperationsSummary(Long accountId, String period) {
        LocalDateTime periodDate = parsePeriodDate(period);
        YearMonth yearMonth = YearMonth.from(periodDate);

        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atStartOfDay();


        BigDecimal totalAmountCalculated = transactionRepository
                .sumAmountByAccountAndPeriod(accountId, startDate, endDate);


        long operationCount = transactionRepository
                .countByAccountIdAndPeriodDateBetween(accountId, startDate, endDate);

        return new OperationSummaryDTO(
                accountId,
                period,
                totalAmountCalculated,
                operationCount
        );
    }

    private LocalDateTime parsePeriodDate(String period) {
        if(period == null || period.isEmpty()) {
            return LocalDateTime.now().withDayOfMonth(1);
        }

        try {
            YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));
            return yearMonth.atEndOfMonth().atTime(23, 59, 59);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid period format. Use YYYY-MM");
        }
    }

}
