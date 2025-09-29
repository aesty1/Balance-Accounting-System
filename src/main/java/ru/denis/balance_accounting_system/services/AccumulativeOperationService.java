package ru.denis.balance_accounting_system.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.dto.*;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;
import ru.denis.balance_accounting_system.repositories.AccountRepository;
import ru.denis.balance_accounting_system.repositories.AccumulativeOperationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccumulativeOperationService {

    @Autowired
    private AccumulativeOperationRepository accumulativeOperationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public AccumulativeResponse processAccumulativeOperation(Long accountId, AccumulativeRequest request) {
        if(request.getReferenceId() != null && accumulativeOperationRepository.existsByReferenceId(request.getReferenceId())) {
            throw new IllegalArgumentException("Accumulative operation already exists");
        }

        Account account = accountRepository.findByIdWithLock(accountId).orElseThrow(() ->
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

        return new AccumulativeResponse (
                savedOperation.getId(),
                accountId,
                request.getAmount(),
                request.getDescription(),
                periodDate,
                updatedAccount.getBalance()
        );
    }


    public BigDecimal calculateComplexAccumulative(BigDecimal baseAmount, BigDecimal rate, int periods, BigDecimal additionalFee) {
        BigDecimal result = baseAmount;

        for (int i = 0; i < periods; i++) {
            BigDecimal interest = result.multiply(rate).setScale(10, RoundingMode.HALF_UP);

            result = result.add(interest).add(additionalFee).setScale(5, RoundingMode.HALF_UP);
        }

        return result;
    }


    public BigDecimal calculateAccumulativeFee(BigDecimal monthlyTurnover, BigDecimal fixedFee, BigDecimal percentageFee) {
        BigDecimal percentageAmount = monthlyTurnover.multiply(percentageFee).setScale(10, RoundingMode.HALF_UP);

        BigDecimal totalFee = fixedFee.add(percentageAmount).setScale(5, RoundingMode.HALF_UP);

        return totalFee;
    }

    public List<AccumulativeOperationDTO> getAccumulativeOperations(Long accountId, String period) {
        LocalDate periodDate = parsePeriodDate(period);

        return accumulativeOperationRepository.findByAccountIdAndPeriodDate(accountId, periodDate)
                .stream()
                .map(AccumulativeOperationDTO::new)
                .collect(Collectors.toList());
    }

    public AccumulativeOperationDTO getAccumulativeOperationWithPrecision(Long operationId) {
        AccumulativeOperation operation = accumulativeOperationRepository.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Accumulative operation not found with id: " + operationId));

        return new AccumulativeOperationDTO(operation);
    }

    public AccumulativeSummaryDTO getAccumulativeSummary(Long accountId, String period) {
        LocalDate periodDate = parsePeriodDate(period);
        YearMonth yearMonth = YearMonth.from(periodDate);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal totalAmountCalculated = accumulativeOperationRepository
                .sumAmountByAccountAndPeriod(accountId, startDate, endDate)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalAmountDisplay = totalAmountCalculated.setScale(2, RoundingMode.HALF_UP);

        long operationCount = accumulativeOperationRepository
                .countByAccountIdAndPeriodDateBetween(accountId, startDate, endDate);

        return new AccumulativeSummaryDTO(
                accountId,
                period,
                totalAmountCalculated,
                totalAmountDisplay,
                operationCount
        );
    }

    public List<AccumulativeCalculationDTO> calculateAccumulativeSeries(BigDecimal baseAmount,
                                                                        BigDecimal rate,
                                                                        int periods) {
        return calculateAccumulativeSeries(baseAmount, rate, periods, BigDecimal.ZERO);
    }

    public List<AccumulativeCalculationDTO> calculateAccumulativeSeries(BigDecimal baseAmount,
                                                                        BigDecimal rate,
                                                                        int periods,
                                                                        BigDecimal additionalFee) {
        List<AccumulativeCalculationDTO> results = new ArrayList<>();
        BigDecimal currentAmount = baseAmount.setScale(5, RoundingMode.HALF_UP);

        for (int i = 1; i <= periods; i++) {
            BigDecimal interest = currentAmount.multiply(rate)
                    .setScale(10, RoundingMode.HALF_UP);

            currentAmount = currentAmount.add(interest)
                    .add(additionalFee)
                    .setScale(5, RoundingMode.HALF_UP);

            AccumulativeCalculationDTO dto = new AccumulativeCalculationDTO();
            dto.setPeriod(i);
            dto.setAmountCalculated(currentAmount);
            dto.setAmountDisplay(currentAmount.setScale(2, RoundingMode.HALF_UP));
            dto.setInterest(interest.setScale(5, RoundingMode.HALF_UP));

            results.add(dto);
        }

        return results;
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
}
