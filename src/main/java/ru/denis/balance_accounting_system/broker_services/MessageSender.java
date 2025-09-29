package ru.denis.balance_accounting_system.broker_services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import ru.denis.balance_accounting_system.dto.AccumulativeRequest;
import ru.denis.balance_accounting_system.dto.AccumulativeResponse;
import ru.denis.balance_accounting_system.dto.OperationType;
import ru.denis.balance_accounting_system.dto.TransactionMessage;
import ru.denis.balance_accounting_system.models.Account;
import ru.denis.balance_accounting_system.models.AccumulativeOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class MessageSender {

    private static final String QUEUE_TRANSACTION_NAME = "transaction.queue";
    private static final String QUEUE_ACCUMULATIVE_NAME = "accumulative_queue";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Приход/расход (базовые операции)

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

    // Накопительное списание
    public void sendAccumulativeOpeation(Long accountId, BigDecimal amount, String description, String period, String referenceId) {
        AccumulativeRequest request = new AccumulativeRequest();

        request.setAccount_id(accountId);
        request.setAmount(amount);
        request.setDescription(description);
        request.setPeriod(period);
        request.setReferenceId(referenceId);

        jmsTemplate.convertAndSend(QUEUE_ACCUMULATIVE_NAME, request);
    }


    private void sendMessage(TransactionMessage message) {
        jmsTemplate.convertAndSend(QUEUE_TRANSACTION_NAME, message);
    }

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
