package com.example.stateful.auth.filter;

import com.example.stateful.auth.context.AuthContext;
import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.security.SessionAuthentication;
import com.example.stateful.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_COOKIE = "AUTH_SESSION_ID";
    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String sessionId = getSessionIdFromRequest(request);

            if (sessionId != null) {
                UserDTO user = authService.authenticate(sessionId);
                SessionAuthentication authentication = new SessionAuthentication(user);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Helpful for the logout controller
                request.setAttribute(AuthContext.SESSION_ID, sessionId);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // If auth logic fails, we clear the context and still let the request
            // proceed (Soft Filter). SecurityConfig will block if path is private.
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            // BULLETPROOF: Ensure the thread is clean when it returns to the pool
            SecurityContextHolder.clearContext();
        }
    }

    private String getSessionIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> SESSION_COOKIE.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
