package com.example.stateful.auth.controller;

import com.example.stateful.auth.dto.request.RegisterRequest;
import com.example.stateful.auth.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String token,
            @RequestBody @Valid RegisterRequest request
    ) {
        registrationService.register(token, request.password());
        return ResponseEntity.ok("User registered successfully");
    }
}

