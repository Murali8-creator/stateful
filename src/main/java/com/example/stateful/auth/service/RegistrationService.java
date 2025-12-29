package com.example.stateful.auth.service;

import com.example.stateful.auth.entity.User;
import com.example.stateful.auth.entity.UserInvitation;
import com.example.stateful.auth.enums.InvitationStatus;
import com.example.stateful.auth.enums.Role;
import com.example.stateful.auth.exception.ForbiddenException;
import com.example.stateful.auth.exception.UnauthorizedException;
import com.example.stateful.auth.repository.InvitationRepository;
import com.example.stateful.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RegistrationService {

    private InvitationStatusService invitationStatusService;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            InvitationStatusService invitationStatusService, InvitationRepository invitationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.invitationStatusService = invitationStatusService;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String token, String rawPassword) {

        UserInvitation invitation =
                invitationRepository.findByToken(token)
                        .orElseThrow(() ->
                                new UnauthorizedException("Invalid invitation token"));

        // ---- validate invitation ----
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ForbiddenException("Invitation already used or expired");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitationStatusService.markAsExpired(invitation); // This commits immediately
            throw new ForbiddenException("Invitation expired"); // The main transaction rolls back, but the status update stays
        }

        // ---- prevent duplicate users ----
        if (userRepository.existsByEmail(invitation.getEmail())) {
            throw new ForbiddenException("User already registered");
        }

        // ---- create user ----
        User user = new User();
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);

        userRepository.save(user);

        // ---- mark invitation used ----
        invitation.setStatus(InvitationStatus.USED);
        invitation.setUsedAt(Instant.now());

        invitationRepository.save(invitation);
    }
}

