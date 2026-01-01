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
    public void handleInvitation(InvitationEvent event) {
        log.info("CONSUMER: Received invitation event for {}", event.email());

        try {
            emailService.sendInvitationEmail(event.email(), event.token());
            log.info("CONSUMER: Email sent successfully to {}", event.email());
        } catch (Exception e) {
            log.error("CONSUMER: Failed to send email to {}: {}", event.email(), e.getMessage());
            // Rethrowing allows RabbitMQ retry/DLQ logic to take over
            throw e;
        }
    }
}
