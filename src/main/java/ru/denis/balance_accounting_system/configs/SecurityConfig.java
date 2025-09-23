package ru.denis.balance_accounting_system.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
@ComponentScan("ru.denis.balance_accounting_system")
public class SecurityConfig {
}
