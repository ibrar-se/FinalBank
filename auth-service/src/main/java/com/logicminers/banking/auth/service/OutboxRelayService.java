package com.logicminers.banking.auth.service;

import com.logicminers.banking.auth.domain.OutboxEvent;
import com.logicminers.banking.auth.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // This lets us print cool logs to the terminal
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // The Clock: Wakes this method up exactly every 10 seconds (10,000 ms)
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void relayEventsToKafka() {

        // 1. Sweep the floor: Find all tickets that haven't been sent yet
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalse();

        if (pendingEvents.isEmpty()) {
            return; // Go back to sleep if the floor is clean
        }

        log.info("Found {} pending events. Firing the Kafka Cannon...", pendingEvents.size());

        // 2. Fire them into the wheel one by one
        for (OutboxEvent event : pendingEvents) {

            // We fire the JSON payload to a Kafka topic named "notification-events"
            kafkaTemplate.send("notification-events", event.getPayload());

            // 3. Mark as done so we NEVER send the same email twice!
            event.setProcessed(true);
            outboxEventRepository.save(event);

            log.info("Successfully fired event ID: {} to Kafka!", event.getId());
        }
    }
}