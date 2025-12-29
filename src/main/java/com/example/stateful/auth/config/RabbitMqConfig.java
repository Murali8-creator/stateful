package com.example.stateful.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMqConfig {

    public static final String INVITATION_QUEUE = "invitation.v1.queue";
    public static final String INVITATION_DLQ = "invitation.v1.dlq";
    public static final String EXCHANGE = "app.events.exchange";
    public static final String DLQ_EXCHANGE = "app.dlq.exchange";

    @Bean
    public DirectExchange appExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Queue invitationQueue() {
        return QueueBuilder.durable(INVITATION_QUEUE)
                // When a message is "rejected" or "nacked", send it here:
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "invitation.dead")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(INVITATION_DLQ).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(dlqExchange())
                .with("invitation.dead");
    }

    @Bean
    public Binding invitationBinding(Queue invitationQueue, DirectExchange appExchange) {
        return BindingBuilder.bind(invitationQueue).to(appExchange).with("invitation.created");
    }

    @Bean
    public MessageConverter jsonConverter(ObjectMapper objectMapper) {
        // Jackson2JsonMessageConverter implements the AMQP MessageConverter interface
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
