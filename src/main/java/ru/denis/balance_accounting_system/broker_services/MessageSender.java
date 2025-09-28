package ru.denis.balance_accounting_system.broker_services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.dto.TransactionMessage;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MessageSender {

    private static final String QUEUE_NAME = "transaction.queue";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendIncomeTransaction(Long accountId, BigDecimal amount, String referenceId) {
        TransactionMessage transactionMessage = new TransactionMessage();

        transactionMessage.setAccountId(accountId);
        transactionMessage.setAmount(amount);
        transactionMessage.setOperationType(OperationType.INCOME);
        transactionMessage.setReferenceId(referenceId);
        transactionMessage.setMessageId(generateMessageId());

        sendMessage(transactionMessage);
    }

    public void sendExpenseTransaction(Long accountId, BigDecimal amount, String referenceId) {
        TransactionMessage transactionMessage = new TransactionMessage();

        transactionMessage.setAccountId(accountId);
        transactionMessage.setAmount(amount);
        transactionMessage.setOperationType(OperationType.EXPENSE);
        transactionMessage.setReferenceId(referenceId);
        transactionMessage.setMessageId(generateMessageId());

        sendMessage(transactionMessage);
    }

    private void sendMessage(TransactionMessage message) {
        jmsTemplate.convertAndSend(QUEUE_NAME, message);
    }

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
