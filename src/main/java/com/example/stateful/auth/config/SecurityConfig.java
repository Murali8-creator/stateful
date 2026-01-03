package com.example.stateful.auth.config;

import com.example.stateful.auth.context.AuthContext;
import com.example.stateful.auth.filter.CsrfCookieFilter;
import com.example.stateful.auth.filter.SessionAuthenticationFilter;
import com.example.stateful.auth.security.CustomAccessDeniedHandler;
import com.example.stateful.auth.security.CustomAuthenticationEntryPoint;
import com.example.stateful.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String SESSION_COOKIE = "AUTH_SESSION_ID";
    private final AuthService authService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(AuthService authService,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.authService = authService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SessionAuthenticationFilter sessionAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(this::configureCsrf)
                .authorizeHttpRequests(this::configureAuthorization)
                .exceptionHandling(this::configureExceptions)
                .logout(this::configureLogout)
                .addFilterBefore(sessionAuthFilter, LogoutFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), SessionAuthenticationFilter.class);

        return http.build();
    }

    private void configureCsrf(CsrfConfigurer<HttpSecurity> csrf) {
        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
    }

    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/auth/login", "/auth/register", "/actuator/**").permitAll()
                .anyRequest().authenticated();
    }

    private void configureExceptions(ExceptionHandlingConfigurer<HttpSecurity> exceptions) {
        exceptions.authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
    }

    private void configureLogout(LogoutConfigurer<HttpSecurity> logout) {
        logout.logoutUrl("/api/auth/logout")
                .addLogoutHandler(this::handleRedisLogout)
                .deleteCookies(SESSION_COOKIE)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK));
    }

    private void handleRedisLogout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        String sessionId = (String) request.getAttribute(AuthContext.SESSION_ID);
        if (sessionId != null) {
            authService.logout(sessionId);
        }
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            var config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:5173"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        };
    }
}
