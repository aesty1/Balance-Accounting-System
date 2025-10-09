package ru.denis.balance_accounting_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.denis.balance_accounting_system.controllers.JmsAccountController;
import ru.denis.balance_accounting_system.dto.JmsTransactionRequest;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableRetry
public class BalanceAccountingSystemApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(BalanceAccountingSystemApplication.class, args);

	}

}
