package com.example.stateful.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter @Setter
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // metadata to help with debugging and routing
    private String eventType;

    // The actual data serialized as JSON
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    // Lifecycle tracking
    private boolean processed = false;
    private int attemptCount = 0;
    private Instant createdAt = Instant.now();
    private Instant lastAttemptedAt;
}


