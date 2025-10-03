package ru.denis.balance_accounting_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.balance_accounting_system.dto.TransactionRequest;
import ru.denis.balance_accounting_system.dto.TransactionResponse;
import ru.denis.balance_accounting_system.dynamic_repositories.TransactionDynamicRepository;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.services.ArchiveService;
import ru.denis.balance_accounting_system.services.BalanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private TransactionDynamicRepository transactionDynamicRepository;

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

    @PostMapping("/archive")
    public ResponseEntity<?> archiveTransactions() {
        archiveService.archiveOldData();

        return ResponseEntity.ok("Old transactions successfully archived");
    }

    @GetMapping("/{date}")
    public ResponseEntity<List<Transaction>> getMonthTransaction(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);

        List<Transaction> transactions = transactionDynamicRepository.findByMonth(localDate);

        return ResponseEntity.ok(transactions);
    }
    
}
