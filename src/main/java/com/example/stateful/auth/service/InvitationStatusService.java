package com.example.stateful.auth.service;

import com.example.stateful.auth.entity.UserInvitation;
import com.example.stateful.auth.enums.InvitationStatus;
import com.example.stateful.auth.repository.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationStatusService {
    private final InvitationRepository invitationRepository;

    public InvitationStatusService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsExpired(UserInvitation invitation) {
        invitation.setStatus(InvitationStatus.EXPIRED);
        invitationRepository.save(invitation);
    }
}

