package com.example.stateful.auth.service;

import com.example.stateful.auth.dto.event.InvitationEvent;
import com.example.stateful.auth.entity.OutboxMessage;
import com.example.stateful.auth.entity.UserInvitation;
import com.example.stateful.auth.enums.InvitationStatus;
import com.example.stateful.auth.repository.InvitationRepository;
import com.example.stateful.auth.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final long INVITE_EXPIRY_MINUTES = 15;

    private final InvitationRepository invitationRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createInvitation(String email, UUID adminUserId) {
        // 1. Check for existing invites
        if (invitationRepository.existsByEmailAndStatus(email, InvitationStatus.PENDING)) {
            throw new IllegalStateException("User already invited");
        }

        // 2. Create the Invitation entity
        UserInvitation invitation = new UserInvitation();
        invitation.setEmail(email);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedAt(Instant.now());
        invitation.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        invitation.setInvitedBy(adminUserId);

        invitationRepository.save(invitation);

        // 3. Create the Event DTO
        InvitationEvent event = new InvitationEvent(email, invitation.getToken(), adminUserId);

        // 4. Save to Outbox (Atomic with the invitation)
        OutboxMessage outbox = new OutboxMessage();
        outbox.setEventType("INVITATION_CREATED");
        try {
            outbox.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
        outboxRepository.save(outbox);
    }
}

