package com.logicminers.banking.fraud.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicminers.banking.fraud.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class FraudEventListener {

    private final FraudDetectionService fraudService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FraudEventListener(FraudDetectionService fraudService, KafkaTemplate<String, String> kafkaTemplate) {
        this.fraudService = fraudService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "fraud-checks", groupId = "fraud-detection-group")
    public void evaluateTransaction(String payload) {
        try {
            // 1. Read the incoming transaction data
            JsonNode node = objectMapper.readTree(payload);
            String transactionId = node.get("transactionId").asText();
            BigDecimal amount = new BigDecimal(node.get("amount").asText());

            log.info("🛡️ Fraud Service analyzing transaction {} for {} SAR...", transactionId, amount);

            // 2. Run the business rules
            boolean isSafe = fraudService.isTransactionSafe(amount);
            String status = isSafe ? "APPROVED" : "REJECTED";

            if (!isSafe) {
                log.warn("🚨 FRAUD ALERT: Transaction {} exceeded limits. Status: REJECTED.", transactionId);
            } else {
                log.info("✅ Transaction {} cleared fraud checks. Status: APPROVED.", transactionId);
            }

            // 3. Send the official verdict back to Kafka
            String replyPayload = """
                    {
                        "transactionId": "%s",
                        "status": "%s"
                    }
                    """.formatted(transactionId, status);

            kafkaTemplate.send("fraud-results", transactionId, replyPayload);

        } catch (Exception e) {
            log.error("CRITICAL: Error processing fraud check!", e);
        }
    }
}