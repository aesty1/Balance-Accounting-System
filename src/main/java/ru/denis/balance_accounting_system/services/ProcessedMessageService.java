package ru.denis.balance_accounting_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denis.balance_accounting_system.repositories.ProcessedMessageRepository;

@Service
public class ProcessedMessageService {

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;
}
