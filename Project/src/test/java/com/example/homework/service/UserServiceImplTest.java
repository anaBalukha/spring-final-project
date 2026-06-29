package com.example.homework.service;

import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.request.UserUpdateRequest;
import com.example.homework.dto.response.UserResponse;
import com.example.homework.entity.Role;
import com.example.homework.entity.User;
import com.example.homework.exception.DuplicateResourceException;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.UserRepository;
import com.example.homework.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("public registration always creates a USER")
    void register_alwaysAssignsUserRole() {
        RegisterRequest request = new RegisterRequest("john", "secret123");

        when(userRepository.existsByUsername("john")).thenReturn(false);

        when(passwordEncoder.encode("secret123")).thenReturn("ENCODED");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserResponse response = userService.register(request);

        assertThat(response.getUsername()).isEqualTo("john");

        assertThat(response.getRole()).isEqualTo(Role.USER);

        verify(userRepository).save(
                argThat(user ->
                        user.getRole() == Role.USER
                                && user.getPassword()
                                .equals("ENCODED")
                )
        );
    }

    @Test
    @DisplayName("registration rejects an existing username")
    void register_duplicateUsername_throws() {
        RegisterRequest request =
                new RegisterRequest("taken", "secret123");

        when(userRepository.existsByUsername("taken"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(request)
        ).isInstanceOf(
                DuplicateResourceException.class
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsername returns the existing user")
    void findByUsername_found() {
        User user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.findByUsername("admin");

        assertThat(response.getUsername())
                .isEqualTo("admin");

        assertThat(response.getRole())
                .isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("findByUsername throws for an unknown user")
    void findByUsername_notFound_throws() {
        when(userRepository.findByUsername("ghost"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.findByUsername("ghost")
        ).isInstanceOf(
                ResourceNotFoundException.class
        );
    }

    // CHANGED: User Read by ID test
    @Test
    @DisplayName("getUserById returns an existing user")
    void getUserById_found() {
        User user = User.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("user");
    }

    // CHANGED: User Update test
    @Test
    @DisplayName("updateUser changes username, password and role")
    void updateUser_success() {
        User user = User.builder()
                .id(1L)
                .username("oldName")
                .password("oldEncoded")
                .role(Role.USER)
                .build();

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "newName",
                        "newPassword",
                        Role.ADMIN
                );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsernameAndIdNot(
                "newName",
                1L
        )).thenReturn(false);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncoded");

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response =
                userService.updateUser(1L, request);

        assertThat(response.getUsername())
                .isEqualTo("newName");

        assertThat(response.getRole())
                .isEqualTo(Role.ADMIN);

        assertThat(user.getPassword())
                .isEqualTo("newEncoded");
    }

    @Test
    @DisplayName("updateUser rejects another user's username")
    void updateUser_duplicateUsername_throws() {
        User user = User.builder()
                .id(1L)
                .username("oldName")
                .password("encoded")
                .role(Role.USER)
                .build();

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "alreadyTaken",
                        null,
                        Role.USER
                );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsernameAndIdNot(
                "alreadyTaken",
                1L
        )).thenReturn(true);

        assertThatThrownBy(() ->
                userService.updateUser(1L, request)
        ).isInstanceOf(
                DuplicateResourceException.class
        );

        verify(userRepository, never()).save(any());
    }

    // CHANGED: User Delete test
    @Test
    @DisplayName("deleteUser deletes an existing user")
    void deleteUser_success() {
        User user = User.builder()
                .id(1L)
                .username("deleteMe")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }
}