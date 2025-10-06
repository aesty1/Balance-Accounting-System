package ru.denis.balance_accounting_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.dto.*;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;
import ru.denis.balance_accounting_system.models.ReserveFund;
import ru.denis.balance_accounting_system.services.AccumulativeOperationService;
import ru.denis.balance_accounting_system.services.BalanceService;
import ru.denis.balance_accounting_system.services.ReserveFundService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AccumulativeOperationService accumulativeOperationService;

    @Autowired
    private ReserveFundService reserveFundService;

    // Приход/расход (базовые операции)

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        BigDecimal balance = balanceService.getBalance(accountId);

        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountId}/balance/{periodStart}/{periodEnd}")
    public ResponseEntity<OperationSummaryDTO> getTransactionSummaryByPeriod(@PathVariable Long accountId, @PathVariable String periodStart, @PathVariable String periodEnd) {
        OperationSummaryDTO operation = balanceService.getOperationsSummary(accountId, periodStart, periodEnd);

        return ResponseEntity.ok(operation);
    }

    @PostMapping("/{accountId}/income")
    public ResponseEntity<TransactionResponse> addIncome(@PathVariable Long accountId, @RequestBody TransactionRequest request) {
        TransactionResponse response = balanceService.addIncome(accountId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/expense")
    public ResponseEntity<TransactionResponse> addExpense(@PathVariable Long accountId, @RequestBody TransactionRequest request) {
        TransactionResponse response = balanceService.addExpense(accountId, request);

        return ResponseEntity.ok(response);
    }

    // Накопительное списание

    @PostMapping("/{accountId}/accumulative")
    public ResponseEntity<AccumulativeResponse> processAccumulative(@PathVariable Long accountId, @RequestBody AccumulativeRequest request) {
        AccumulativeResponse response = accumulativeOperationService.processAccumulativeOperation(accountId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/accumulative/calculate")
    public ResponseEntity<BigDecimal> calculateComplexAccumulative(@RequestBody CalculationRequest request) {
        BigDecimal result = accumulativeOperationService.calculateComplexAccumulative(
                request.getBaseAmount(),
                request.getRate(),
                request.getPeriods(),
                request.getAdditionalFee()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accountId}/accumulative/{period}")
    public ResponseEntity<List<AccumulativeOperationDTO>> getAccumulativeOperations(@PathVariable Long accountId, @PathVariable String period) {
        List<AccumulativeOperationDTO> operations = accumulativeOperationService.getAccumulativeOperations(accountId, period);

        return ResponseEntity.ok(operations);
    }

    // Резервный фонд

    @GetMapping("/{accountId}/reserve")
    public ResponseEntity<List<ReserveFundResponse>> getReserveFund(@PathVariable Long accountId) {
        List<ReserveFundResponse> reserveFundList =  reserveFundService.getAllById(accountId);

        return ResponseEntity.ok(reserveFundList);
    }

    @PostMapping("/{accountId}/reserve/income")
    public ResponseEntity<String> reserveFundIncome(@PathVariable Long accountId, @RequestBody TransactionRequest request) {
        reserveFundService.addReserveIncome(accountId, request);

        return ResponseEntity.ok("Income transaction to reserve fund sent");
    }

    @PostMapping("/{accountId}/reserve/expense")
    public ResponseEntity<String> reserveFundExpense(@PathVariable Long accountId, @RequestBody TransactionRequest request) {
        reserveFundService.addReserveExpense(accountId, request);

        return ResponseEntity.ok("Expense transaction to reserve fund sent");
    }
}
