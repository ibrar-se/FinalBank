package com.logicminers.banking.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // E.g., "USER"
    @Column(nullable = false)
    private String aggregateType;

    // E.g., "USER_REGISTERED"
    @Column(nullable = false)
    private String eventType;

    // The actual JSON data (User ID, Email, Token) we will send to Kafka
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    // Has the background worker pushed this to Kafka yet?
    @Column(nullable = false)
    private boolean processed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}