package com.example.stateful.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Exchanges
    public static final String EXCHANGE = "app.events.exchange";
    public static final String DLQ_EXCHANGE = "app.dlq.exchange";

    // Invitation Queues & Keys
    public static final String INVITATION_QUEUE = "invitation.v1.queue";
    public static final String INVITATION_DLQ = "invitation.v1.dlq";
    public static final String INVITATION_ROUTING_KEY = "invitation.created";
    public static final String INVITATION_DEAD_LETTER_KEY = "invitation.dead";

    // ML Processing Queues & Keys
    public static final String ML_QUEUE = "ml.processing.queue";
    public static final String ML_DLQ = "ml.v1.dlq";
    public static final String ML_ROUTING_KEY = "ml.process.start";
    public static final String ML_DEAD_LETTER_KEY = "ml.dead";

    // ML Result Queues & Keys
    public static final String ML_RESULTS_QUEUE = "ml.results.queue";
    public static final String ML_RESULTS_ROUTING_KEY = "ml.process.finished";

    // --- Exchanges ---

    @Bean
    public DirectExchange appExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    // --- Invitation Queues ---

    @Bean
    public Queue invitationQueue() {
        return QueueBuilder.durable(INVITATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVITATION_DEAD_LETTER_KEY)
                .build();
    }

    @Bean
    public Queue invitationDeadLetterQueue() {
        return QueueBuilder.durable(INVITATION_DLQ).build();
    }

    // --- ML Queues ---

    @Bean
    public Queue mlQueue() {
        return QueueBuilder.durable(ML_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ML_DEAD_LETTER_KEY)
                .build();
    }

    @Bean
    public Queue mlDeadLetterQueue() {
        return QueueBuilder.durable(ML_DLQ).build();
    }

    @Bean
    public Queue mlResultsQueue() {
        return QueueBuilder.durable(ML_RESULTS_QUEUE).build();
    }

    // --- Bindings (Explicitly named parameters to avoid ambiguity) ---

    @Bean
    public Binding invitationBinding(Queue invitationQueue, DirectExchange appExchange) {
        return BindingBuilder.bind(invitationQueue)
                .to(appExchange)
                .with(INVITATION_ROUTING_KEY);
    }

    @Bean
    public Binding invitationDlqBinding(Queue invitationDeadLetterQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(invitationDeadLetterQueue)
                .to(dlqExchange)
                .with(INVITATION_DEAD_LETTER_KEY);
    }

    @Bean
    public Binding mlBinding(Queue mlQueue, DirectExchange appExchange) {
        return BindingBuilder.bind(mlQueue)
                .to(appExchange)
                .with(ML_ROUTING_KEY);
    }

    @Bean
    public Binding mlDlqBinding(Queue mlDeadLetterQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(mlDeadLetterQueue)
                .to(dlqExchange)
                .with(ML_DEAD_LETTER_KEY);
    }

    @Bean
    public Binding mlResultsBinding(Queue mlResultsQueue, DirectExchange appExchange) {
        return BindingBuilder.bind(mlResultsQueue)
                .to(appExchange)
                .with(ML_RESULTS_ROUTING_KEY);
    }

    // --- Converter ---

    @Bean
    public MessageConverter jsonConverter(ObjectMapper objectMapper) {
        // Jackson2JsonMessageConverter implements the AMQP MessageConverter interface
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
