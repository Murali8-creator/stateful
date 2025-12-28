package com.example.stateful.auth.service;


import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.entity.User;
import com.example.stateful.auth.exception.EntityNotFoundException;
import com.example.stateful.auth.mapper.UserMapper;
import com.example.stateful.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toDTO(user);
    }
}
