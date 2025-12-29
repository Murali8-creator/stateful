package com.example.stateful.auth.repository;

import com.example.stateful.auth.entity.UserInvitation;
import com.example.stateful.auth.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<UserInvitation, UUID> {

    Optional<UserInvitation> findByToken(String token);

    boolean existsByEmailAndStatus(String email, InvitationStatus status);
}

