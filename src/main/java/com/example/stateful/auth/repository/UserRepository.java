package com.example.stateful.auth.repository;

import com.example.stateful.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);

    Optional<User> findById(UUID uuid);

    boolean existsByEmail(String email);
}
