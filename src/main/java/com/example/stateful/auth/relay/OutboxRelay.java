package com.example.stateful.auth.relay;

import com.example.stateful.auth.config.RabbitMqConfig;
import com.example.stateful.auth.entity.OutboxMessage;
import com.example.stateful.auth.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional // CRITICAL: The lock exists ONLY as long as this transaction is open
    public void relayMessages() {
        // We only take 10 messages at a time to keep transactions short
        List<OutboxMessage> messages = outboxRepository.findUnprocessedWithLock(PageRequest.of(0, 10));

        if (messages.isEmpty()) return;

        log.info("Relay found {} messages to process", messages.size());

        for (OutboxMessage msg : messages) {


            try {

                String routingKey = switch (msg.getEventType()) {
                    case "INVITATION_CREATED" -> "invitation.created";
                    case "ML_PROCESS_START"   -> "ml.process.start";
                    default -> throw new IllegalArgumentException("Unknown event type: " + msg.getEventType());
                };

                // Create a proper AMQP Message with JSON content-type manually
                Message message = new Message(
                        msg.getPayload().getBytes(),
                        new MessageProperties()
                );
                message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);

                // 1. Send to RabbitMQ
                rabbitTemplate.convertAndSend(
                        "app.events.exchange",
                        routingKey,
                        message
                );

                // 2. Mark as processed in the SAME transaction
                msg.setProcessed(true);
                outboxRepository.save(msg);

            } catch (Exception e) {
                log.error("Failed to relay message {}: {}", msg.getId(), e.getMessage());
                // If this fails, the row remains processed=false and the lock is released on rollback
            }
        }
    }
}
