package ru.denis.balance_accounting_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.dto.TransactionRequest;
import ru.denis.balance_accounting_system.dto.TransactionResponse;
import ru.denis.balance_accounting_system.services.BalanceService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private BalanceService balanceService;

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

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        BigDecimal balance = balanceService.getBalance(accountId);

        return ResponseEntity.ok(balance);
    }
}
