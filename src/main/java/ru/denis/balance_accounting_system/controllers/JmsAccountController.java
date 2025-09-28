package ru.denis.balance_accounting_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.broker_services.MessageSender;
import ru.denis.balance_accounting_system.dto.JmsTransactionRequest;

@RestController
@RequestMapping("/api/jms/accounts")
public class JmsAccountController {

    @Autowired
    private MessageSender messageSender;

    @PostMapping("/{accountId}/income")
    public ResponseEntity<String> sendIncomeTransaction(@PathVariable Long accountId, @RequestBody JmsTransactionRequest request) {
        messageSender.sendIncomeTransaction(
                accountId,
                request.getAmount(),
                request.getReferenceId()
        );

        return ResponseEntity.ok("Income transaction sent to queue for account: " + accountId);
    }

    @PostMapping("/{accountId}/expense")
    public ResponseEntity<String> sendExpenseTransaction(@PathVariable Long accountId, @RequestBody JmsTransactionRequest request) {
        messageSender.sendExpenseTransaction(
                accountId,
                request.getAmount(),
                request.getReferenceId()
        );

        return ResponseEntity.ok("Expense transaction sent to queue for account: " + accountId);
    }
}
