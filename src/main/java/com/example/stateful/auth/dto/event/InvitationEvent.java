package com.example.stateful.auth.dto.event;

import java.util.UUID;

public record InvitationEvent(
        String email,
        String token,
        UUID invitedBy
) {}
