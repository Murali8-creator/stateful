package com.example.stateful.auth.security;

import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.enums.Role;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OwnershipChecker {

    public boolean isOwner(UUID resourceUserId, UserDTO currentUser) {
        return resourceUserId.equals(currentUser.id());
    }

    public boolean isAdmin(UserDTO user) {
        return user.role() == Role.ADMIN;
    }
}

