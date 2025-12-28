package com.example.stateful.auth.dto.response;

import com.example.stateful.auth.enums.Role;

import java.util.UUID;

public record UserDTO(UUID id, String email, Role role) {
}
