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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SESSION_COOKIE = "AUTH_SESSION_ID";
    private static final String XSRF_COOKIE = "XSRF-TOKEN";

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


//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
//        // 1. Invalidate the session in Redis
//        String sessionId = (String) request.getAttribute(AuthContext.SESSION_ID);
//        if (sessionId != null) {
//            authService.logout(sessionId);
//        }
//        SecurityContextHolder.clearContext();
//
//        // 2. Clear AUTH_SESSION_ID
//        clearCookie(response, SESSION_COOKIE, true);
//
//        // 3. Clear XSRF-TOKEN
//        // IMPORTANT: It must be httpOnly(false) so it matches the CSRF repo settings
//        clearCookie(response, XSRF_COOKIE, false);
//
//        return ResponseEntity.ok().build();
//    }
//
//    private void clearCookie(HttpServletResponse response, String name, boolean httpOnly) {
//        Cookie cookie = new Cookie(name, ""); // Use empty string instead of null
//        cookie.setPath("/");
//        cookie.setHttpOnly(httpOnly);
//        cookie.setMaxAge(0); // This tells the browser to delete it
//
//        // If you are using Chrome/Modern browsers, ensure the SameSite attribute
//        // doesn't block the deletion if you're in a cross-origin setup.
//        response.addCookie(cookie);
//    }
}
