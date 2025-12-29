package com.example.stateful.auth.controller;

import com.example.stateful.auth.context.AuthContext;
import com.example.stateful.auth.dto.request.LoginRequest;
import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.exception.UnauthorizedException;
import com.example.stateful.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SESSION_COOKIE = "AUTH_SESSION_ID";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        String sessionId = authService.login(
                loginRequest.email(),
                loginRequest.password()
        );

        Cookie cookie = new Cookie(SESSION_COOKIE, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> profile(
            @AuthenticationPrincipal UserDTO user
    ) {
        return ResponseEntity.ok(user);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId =
                (String) request.getAttribute(AuthContext.SESSION_ID);

        authService.logout(sessionId);

        // Expire cookie
        Cookie cookie = new Cookie(SESSION_COOKIE, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }
}
