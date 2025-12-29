package com.example.stateful.auth.controller;

import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.service.InvitationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/invitations")
public class AdminInvitationController {

    private final InvitationService invitationService;

    public AdminInvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> inviteUser(
            @RequestParam String email,
            @AuthenticationPrincipal UserDTO admin
    ) throws JsonProcessingException {
        invitationService.createInvitation(email, admin.id());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Invitation sent");
    }
}
