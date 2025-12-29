package com.example.stateful.auth.service;

import com.example.stateful.auth.dto.Session;
import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.entity.User;
import com.example.stateful.auth.exception.EntityNotFoundException;
import com.example.stateful.auth.exception.ForbiddenException;
import com.example.stateful.auth.exception.UnauthorizedException;
import com.example.stateful.auth.mapper.UserMapper;
import com.example.stateful.auth.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final long IDLE_TIMEOUT_SECONDS = 15 * 60;
    private static final String SESSION_PREFIX = "auth:session:";

    private final RedisTemplate<String, Object> redisTemplate;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService(
            RedisTemplate<String, Object> redisTemplate,
            PasswordEncoder passwordEncoder, UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String sessionId = UUID.randomUUID().toString();
        String redisKey = SESSION_PREFIX + sessionId;

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(user.getId());
        session.setCreatedAt(Instant.now());

        redisTemplate.opsForValue()
                .set(redisKey, session, IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return sessionId;
    }

    public void logout(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    public UserDTO authenticate(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;

        Session session = (Session) redisTemplate.opsForValue().get(sessionKey);

        if (session == null) {
            throw new ForbiddenException("Session expired or invalid");
        }

        // sliding TTL refresh
        redisTemplate.expire(sessionKey, IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toDTO(user);
    }
}
