package com.example.stateful.auth.dto.request;

public record LoginRequest(
        String email,
        String password) { }
