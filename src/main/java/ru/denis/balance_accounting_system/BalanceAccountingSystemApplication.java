package ru.denis.balance_accounting_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class BalanceAccountingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BalanceAccountingSystemApplication.class, args);
	}

}
