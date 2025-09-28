package ru.denis.balance_accounting_system.broker_services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.dto.ProcessStatus;
import ru.denis.balance_accounting_system.dto.TransactionMessage;
import ru.denis.balance_accounting_system.dto.TransactionRequest;
import ru.denis.balance_accounting_system.models.ProcessedMessage;
import ru.denis.balance_accounting_system.repositories.ProcessedMessageRepository;
import ru.denis.balance_accounting_system.services.BalanceService;

@Component
public class MessageReceiver {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @JmsListener(destination = "transaction.queue")
    @Transactional
    public void processTransaction(@Payload TransactionMessage message) {
        // Проверка на идемпотентность
        if(processedMessageRepository.existsByMessageId(message.getMessageId())) {
            return;
        }

        try {
            TransactionRequest request = new TransactionRequest();

            request.setAmount(message.getAmount());
            request.setReferenceId(message.getReferenceId());
            request.setDescription("JMS transaction: " + message.getOperationType());

            if(message.getOperationType() == OperationType.INCOME) {
                balanceService.addIncome(message.getAccountId(), request);
            } else if(message.getOperationType() == OperationType.EXPENSE) {
                balanceService.addExpense(message.getAccountId(), request);
            }

            saveProcessedMessage(message, "PROCESSED");
        } catch (Exception e) {
            saveProcessedMessage(message, "ERROR");
            throw new RuntimeException("Error processing JMS message", e);
        }
    }

    private void saveProcessedMessage(TransactionMessage message, String status) {
        ProcessedMessage processedMessage = new ProcessedMessage();

        processedMessage.setAccountId(message.getAccountId());
        processedMessage.setMessageId(message.getMessageId());
        processedMessage.setMessageType("TRANSACTION");
        processedMessage.setStatus(ProcessStatus.valueOf(status));

        processedMessageRepository.save(processedMessage);
    }
}
