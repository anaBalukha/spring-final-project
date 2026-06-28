package com.example.homework.service.impl;

import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.response.UserResponse;
import com.example.homework.entity.Role;
import com.example.homework.entity.User;
import com.example.homework.exception.DuplicateResourceException;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.UserRepository;
import com.example.homework.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        log.debug("Registration attempt for username {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("error.user.username.exists", request.getUsername());
        }
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        User saved = userRepository.save(user);
        log.info("User registered: username={}, role={}", saved.getUsername(), saved.getRole());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return toResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.notfound", username)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.debug("Fetched {} user(s)", users.size());
        return users;
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
