package ru.denis.balance_accounting_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.dto.TransactionRequest;
import ru.denis.balance_accounting_system.services.BalanceService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private BalanceService balanceService;

    @DeleteMapping("/{referenceId}")
    public ResponseEntity<?> removeOperation(@PathVariable String referenceId) {
        balanceService.removeOperation(referenceId);

        return ResponseEntity.ok("Operation successfully removed");
    }

    @PutMapping("/{referenceId}")
    public ResponseEntity<?> editOperation(@PathVariable String referenceId, @RequestBody TransactionRequest request) {
        balanceService.editOperation(referenceId, request);

        return ResponseEntity.ok("Operation successfully edited");
    }
}
