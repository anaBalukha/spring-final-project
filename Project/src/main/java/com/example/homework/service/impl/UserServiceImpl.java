package com.example.homework.service.impl;

import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.request.UserUpdateRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        log.debug(
                "Registration attempt for username {}",
                request.getUsername()
        );

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "error.user.username.exists",
                    request.getUsername()
            );
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))

                // The client can no longer choose ADMIN.
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);

        log.info(
                "User registered: username={}, role={}",
                saved.getUsername(),
                saved.getRole()
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "error.user.notfound",
                                username
                        )
                );

        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        log.debug("Fetched {} user(s)", users.size());

        return users;
    }

    // CHANGED: User Read by ID
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    // CHANGED: User Update
    @Override
    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request
    ) {
        User user = findById(id);

        if (userRepository.existsByUsernameAndIdNot(
                request.getUsername(),
                id
        )) {
            throw new DuplicateResourceException(
                    "error.user.username.exists",
                    request.getUsername()
            );
        }

        user.setUsername(request.getUsername());
        user.setRole(request.getRole());

        /*
         * A null password means:
         * keep the existing encoded password.
         */
        if (request.getPassword() != null) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User saved = userRepository.save(user);

        log.info(
                "User updated: id={}, username={}, role={}",
                saved.getId(),
                saved.getUsername(),
                saved.getRole()
        );

        return toResponse(saved);
    }

    // CHANGED: User Delete
    @Override
    public void deleteUser(Long id) {
        User user = findById(id);

        userRepository.delete(user);

        log.info(
                "User deleted: id={}, username={}",
                user.getId(),
                user.getUsername()
        );
    }

    // CHANGED:
    // Shared lookup prevents duplicated findById code.
    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "error.user.id.notfound",
                                id
                        )
                );
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}