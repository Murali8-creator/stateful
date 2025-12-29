package com.example.stateful.auth.service.impl;

import com.example.stateful.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendInvitationEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("You've been invited!");
            message.setText("To complete your registration, click here: " +
                    "http://localhost:8081/auth/register?token=" + token);

            mailSender.send(message);
            log.info("Email successfully sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // We rethrow so the RabbitMQ Consumer knows to retry later
            throw new RuntimeException("Email delivery failed", e);
        }
    }
}
