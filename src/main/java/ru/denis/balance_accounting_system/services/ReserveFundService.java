package ru.denis.balance_accounting_system.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.ReserveFundResponse;
import ru.denis.balance_accounting_system.dto.TransactionRequest;
import ru.denis.balance_accounting_system.dto.TransactionResponse;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ReserveFund;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.ReserveFundRepository;

import java.util.List;

@Service
public class ReserveFundService {

    @Autowired
    private ReserveFundRepository reserveFundRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceService balanceService;

    public void addReserveIncome(Long accountId, TransactionRequest request) {
        TransactionResponse response = balanceService.addExpense(accountId, request);

        if(response == null) {
            return;
        }

        ReserveFund reserveFund = new ReserveFund();

        reserveFund.setAccountId(accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found")));
        reserveFund.setAmount(request.getAmount());
        reserveFund.setDescription(request.getDescription());
        reserveFund.setReferenceId(request.getReferenceId());

        reserveFundRepository.save(reserveFund);
    }

    public void addReserveExpense(Long accountId, TransactionRequest request) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found"));

        ReserveFund reserveFund = reserveFundRepository.findByAccountIdAndReferenceId(account, request.getReferenceId()).orElseThrow(() -> new EntityNotFoundException("Reserve fund not found"));

        reserveFund.setAmount(reserveFund.getAmount().subtract(request.getAmount()));

        reserveFundRepository.save(reserveFund);
    }

    public List<ReserveFundResponse> getAllById(Long accountId) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found"));

        List<ReserveFund> reserveFundList = reserveFundRepository.findAllByAccountId(account);

        return reserveFundList.stream().map(this::convertToDto).toList();
    }

    private ReserveFundResponse convertToDto(ReserveFund reserveFund) {
        ReserveFundResponse reserveFundResponse = new ReserveFundResponse();

        reserveFundResponse.setId(reserveFund.getId());
        reserveFundResponse.setAccountId(reserveFund.getAccountId().getId());
        reserveFundResponse.setAmount(reserveFund.getAmount());
        reserveFundResponse.setCreatedAt(reserveFund.getCreatedAt());
        reserveFundResponse.setDescription(reserveFund.getDescription());
        reserveFundResponse.setReferenceId(reserveFund.getReferenceId());

        return reserveFundResponse;
    }
}
