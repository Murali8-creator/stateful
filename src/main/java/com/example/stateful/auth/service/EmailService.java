package com.example.stateful.auth.service;

public interface EmailService {
    void sendInvitationEmail(String to, String token);
}
