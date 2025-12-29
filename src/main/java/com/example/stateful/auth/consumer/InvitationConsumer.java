package com.example.stateful.auth.consumer;

import com.example.stateful.auth.config.RabbitMqConfig;
import com.example.stateful.auth.dto.event.InvitationEvent;
import com.example.stateful.auth.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationConsumer {

    private final ObjectMapper objectMapper;
     private final EmailService emailService;

    @RabbitListener(queues = RabbitMqConfig.INVITATION_QUEUE)
    public void handleInvitation(String messagePayload) throws JsonProcessingException {
        // Note: No try-catch here!
        // We WANT the exception to propagate up so Spring knows it failed
        // and triggers the retry/DLQ logic.

        InvitationEvent event = objectMapper.readValue(messagePayload, InvitationEvent.class);

        log.info("CONSUMER: Attempting email to {}", event.email());

        emailService.sendInvitationEmail(event.email(), event.token());
    }
}
