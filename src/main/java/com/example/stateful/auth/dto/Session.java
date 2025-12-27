package com.example.stateful.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Session {

    private String sessionId;
    private UUID userId;
    private Instant createdAt;
}
