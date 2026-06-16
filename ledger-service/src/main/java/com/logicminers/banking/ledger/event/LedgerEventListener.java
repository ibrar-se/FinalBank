package com.logicminers.banking.ledger.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.logicminers.banking.ledger.domain.EntryType;
import com.logicminers.banking.ledger.domain.LedgerEntry;
import com.logicminers.banking.ledger.repository.LedgerEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LedgerEventListener {

    private final LedgerEntryRepository ledgerRepository;
    private final ObjectMapper objectMapper;

    public LedgerEventListener(LedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
        // Setting up the tool to safely read the JSON text into our Record
        this.objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule());
    }

    @KafkaListener(topics = "ledger-events", groupId = "ledger-service-group")
    public void handleLedgerEvent(String payload) {
        log.info("Ledger Service intercepted a completed transaction: [{}]", payload);

        try {
            // 1. Read the JSON
            TransactionCompletedEvent event = objectMapper.readValue(payload, TransactionCompletedEvent.class);

            // 2. Create the DEBIT row for the sender (Money leaving)
            LedgerEntry debitEntry = LedgerEntry.builder()
                    .transactionId(event.transactionId())
                    .accountNumber(event.sourceAccount())
                    .amount(event.amount())
                    .entryType(EntryType.DEBIT)
                    .build();

            // 3. Create the CREDIT row for the receiver (Money arriving)
            LedgerEntry creditEntry = LedgerEntry.builder()
                    .transactionId(event.transactionId())
                    .accountNumber(event.targetAccount())
                    .amount(event.amount())
                    .entryType(EntryType.CREDIT)
                    .build();

            // 4. Save BOTH permanent records to the database instantly
            ledgerRepository.saveAll(List.of(debitEntry, creditEntry));

            log.info("Double-Entry recorded successfully for Transaction ID: {}", event.transactionId());

        } catch (Exception e) {
            log.error("CRITICAL: Ledger failed to record transaction! Error: {}", e.getMessage());
        }
    }
}