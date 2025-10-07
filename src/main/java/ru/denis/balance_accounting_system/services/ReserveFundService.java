package ru.denis.balance_accounting_system.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.*;
import ru.denis.balance_accounting_system.dynamic_repositories.ReserveFundDynamicRepository;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.ReserveFund;
import ru.denis.balance_accounting_system.models.Transaction;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.ReserveFundRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReserveFundService {

    @Autowired
    private ReserveFundRepository reserveFundRepository;

    @Autowired
    private ReserveFundDynamicRepository reserveFundDynamicRepository;

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

        reserveFundDynamicRepository.save(reserveFund);
    }

    public void returnReserve(Long accountId, TransactionRequest request) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found"));

        ReserveFund reserveFund = reserveFundRepository.findByAccountIdAndReferenceId(account, request.getReferenceId()).orElseThrow(() -> new EntityNotFoundException("Reserve fund not found"));

        if(reserveFund.getAmount().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Reserve fund dont have too much money");
        }
        request.setReferenceId(request.getReferenceId() + UUID.randomUUID().toString().substring(0, 2));
        balanceService.addIncome(accountId, request);

        reserveFund.setAmount(reserveFund.getAmount().subtract(request.getAmount()));

        reserveFundDynamicRepository.save(reserveFund);
    }

    public void addReserveExpense(Long accountId, TransactionRequest request) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found"));

        ReserveFund reserveFund = reserveFundRepository.findByAccountIdAndReferenceId(account, request.getReferenceId()).orElseThrow(() -> new EntityNotFoundException("Reserve fund not found"));

        reserveFund.setAmount(reserveFund.getAmount().subtract(request.getAmount()));

        reserveFundDynamicRepository.save(reserveFund);
    }

    public List<ReserveFundResponse> getAllById(Long accountId) {
        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() -> new EntityNotFoundException("Account not found"));

        List<ReserveFund> reserveFundList = reserveFundRepository.findAllByAccountId(account);

        return reserveFundList.stream().map(this::convertToDto).toList();
    }

    public ReserveFundSummaryDTO getReserveFundSummary(Long accountId, String dateStartText, String dateEndText) {
        YearMonth startYearMonth = YearMonth.parse(dateStartText);
        YearMonth endYearMonth = YearMonth.parse(dateEndText);

        LocalDate startDate = startYearMonth.atDay(1);
        LocalDate endDate = endYearMonth.atEndOfMonth();


        List<ReserveFund> reserveFunds = reserveFundDynamicRepository.findByPeriod(startDate, endDate);

        if(reserveFunds.isEmpty()) {
            return new ReserveFundSummaryDTO();
        }

        BigDecimal totalAmountCalculated = reserveFunds.stream()
                .map(ReserveFund::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long operationCount = reserveFunds.size();

        List<ReserveFundResponse> response = reserveFunds.stream()
                .map(reserveFundList -> new ReserveFundResponse(
                        reserveFundList.getId(),
                        reserveFundList.getAccountId().getId(),
                        reserveFundList.getAmount(),
                        reserveFundList.getDescription(),
                        reserveFundList.getReferenceId(),
                        reserveFundList.getCreatedAt()
                ))
                .toList();

        return new ReserveFundSummaryDTO(
                accountId,
                totalAmountCalculated,
                operationCount,
                response

        );


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
