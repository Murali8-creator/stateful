package com.example.stateful.auth.filter;

import com.example.stateful.auth.context.AuthContext;
import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.exception.ForbiddenException;
import com.example.stateful.auth.exception.UnauthorizedException;
import com.example.stateful.auth.security.SessionAuthentication;
import com.example.stateful.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_COOKIE = "AUTH_SESSION_ID";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/actuator/health"
    );

    private final AuthService authService;

    public SessionAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String sessionId = extractSessionId(request);

            UserDTO user = authService.authenticate(sessionId);

            SessionAuthentication authentication =
                    new SessionAuthentication(user);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException ex) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        }finally {
            SecurityContextHolder.clearContext();
        }

    }

    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new UnauthorizedException("Missing authentication cookie");
        }

        return Arrays.stream(cookies)
                .filter(c -> SESSION_COOKIE.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new UnauthorizedException("Missing authentication cookie"));
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        response.getWriter().write(
                """
                {
                  "error": "%s"
                }
                """.formatted(message)
        );
    }
}
