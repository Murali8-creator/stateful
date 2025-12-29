package com.example.stateful.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ml_tasks")
@Getter
@Setter
@NoArgsConstructor
public class MLTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // We will use the task_id from JSON

    private String inputData;

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED, FAILED

    private String result;
    private Instant createdAt = Instant.now();

    public enum TaskStatus { PENDING, COMPLETED, FAILED }
}
