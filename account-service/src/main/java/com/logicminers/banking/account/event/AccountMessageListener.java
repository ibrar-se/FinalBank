package com.logicminers.banking.account.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.logicminers.banking.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountMessageListener {

    private final AccountService accountService;
    private final KafkaTemplate<String, String> kafkaTemplate; // Changed to String
    private final ObjectMapper objectMapper; // Added this field

    public AccountMessageListener(AccountService accountService, KafkaTemplate<String, String> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
        // Initialize the tool that converts JSON text to Java Objects
        this.objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "account-commands", groupId = "account-service-group")
    public void handleAccountCommand(String payload) {
        log.info("Received Raw Kafka Command: [{}]", payload);

        try {
            // 1. Manually parse the JSON string
            TransactionEvent event = objectMapper.readValue(payload, TransactionEvent.class);

            // 2. Business Logic
            if ("DEBIT".equals(event.action())) {
                accountService.debitAccount(event.accountNumber(), event.amount());
            } else if ("CREDIT".equals(event.action())) {
                accountService.creditAccount(event.accountNumber(), event.amount());
            }

            // 3. Build the SUCCESS reply JSON as a String
            String successReply = """
                    {
                        "transactionId": "%s",
                        "accountNumber": "%s",
                        "amount": %s,
                        "action": "%s",
                        "status": "SUCCESS",
                        "message": "Operation successful"
                    }
                    """.formatted(event.transactionId(), event.accountNumber(), event.amount(), event.action());

            kafkaTemplate.send("transaction-results", event.transactionId().toString(), successReply);
            log.info("Successfully processed {}. Sent SUCCESS reply.", event.action());

        } catch (Exception e) {
            log.error("Failed to process command: {}", e.getMessage());

            // 4. Send FAILED reply as a String
            String failedReply = """
                    {
                        "transactionId": "UNKNOWN",
                        "status": "FAILED",
                        "message": "Error: %s"
                    }
                    """.formatted(e.getMessage());
            kafkaTemplate.send("transaction-results", "error", failedReply);
        }
    }
}