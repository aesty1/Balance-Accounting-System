package ru.denis.balance_accounting_system.configs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.denis.balance_accounting_system.repositories.AccountRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class BalanceMetrics {

    private final Counter incomeTransactionsCounter;
    private final Counter expenseTransactionsCounter;
    private final Counter accumulativeTransactionsCounter;
    private final Counter jmsMessagesCounter;

    private final Timer incomeProcessingTimer;
    private final Timer expenseProcessingTimer;
    private final Timer accumulativeProcessingTimer;

    private final MeterRegistry meterRegistry;
    private final AccountRepository accountRepository;
    private final AtomicReference<Double> totalBalance;

    public BalanceMetrics(MeterRegistry meterRegistry, AccountRepository accountRepository) {
        this.meterRegistry = meterRegistry;
        this.accountRepository = accountRepository;
        this.totalBalance = new AtomicReference<>(0.0);

        this.incomeTransactionsCounter = Counter.builder("balance.transactions.income")
                .description("Count of income transactions")
                .register(meterRegistry);

        this.expenseTransactionsCounter = Counter.builder("balance.transactions.expense")
                .description("Count of expense transactions")
                .register(meterRegistry);

        this.accumulativeTransactionsCounter = Counter.builder("balance.transactions.accumulative")
                .description("Count of accumulative transactions")
                .register(meterRegistry);

        this.jmsMessagesCounter = Counter.builder("balance.jms.messages")
                .description("Count of JMS messages processed")
                .register(meterRegistry);

        this.incomeProcessingTimer = Timer.builder("balance.processing.time.income")
                .description("Time taken to process income transactions")
                .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                )
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.expenseProcessingTimer = Timer.builder("balance.processing.time.expense")
                .description("Time taken to process expense transactions")
                .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                )
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.accumulativeProcessingTimer = Timer.builder("balance.processing.time.accumulative")
                .description("Time taken to process accumulative transactions")
                .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                )
                .publishPercentileHistogram()
                .register(meterRegistry);

        Gauge.builder("balance.accounts.total", totalBalance, AtomicReference::get)
                .description("Total balance across all accounts")
                .register(meterRegistry);
    }

    public void incrementIncomeTransactions() {
        incomeTransactionsCounter.increment();
    }

    public void incrementExpenseTransactions() {
        expenseTransactionsCounter.increment();
    }


    public void incrementAccumulativeTransactions() {
        System.out.println("📊 INCREMENTING ACCUMULATIVE TRANSACTION COUNTER");
        accumulativeTransactionsCounter.increment();
        System.out.println("📊 Current accumulative count: " + getAccumulativeCount());
    }

    public long getAccumulativeCount() {
        return (long) accumulativeTransactionsCounter.count();
    }

    public void stopAccumulativeTimer(Timer.Sample sample) {
        System.out.println("⏱️ STOPPING ACCUMULATIVE TIMER");
        sample.stop(accumulativeProcessingTimer);
        System.out.println("⏱️ Accumulative timer stopped");
    }

    public void incrementJmsMessages() {
        jmsMessagesCounter.increment();
    }

    public Timer.Sample startIncomeTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopIncomeTimer(Timer.Sample sample) {
        sample.stop(incomeProcessingTimer);
    }

    public Timer.Sample startExpenseTimer() {
        return Timer.start(meterRegistry);
    }

    public long getExpenseCount() {
        return (long) expenseTransactionsCounter.count();
    }

    public void stopExpenseTimer(Timer.Sample sample) {
        sample.stop(expenseProcessingTimer);
    }

    public Timer.Sample startAccumulativeTimer() {
        return Timer.start(meterRegistry);
    }


    @Scheduled(fixedRate = 30000)
    public void updateBalanceMetrics() {
        try {
            List<ru.denis.balance_accounting_system.models.Account> accounts = accountRepository.findAll();
            BigDecimal total = BigDecimal.ZERO;
            for (ru.denis.balance_accounting_system.models.Account account : accounts) {
                if (account.getBalance() != null) {
                    total = total.add(account.getBalance());
                }
            }
            totalBalance.set(total.doubleValue());
        } catch (Exception e) {
            System.err.println("Error updating balance metrics: " + e.getMessage());
        }
    }
}