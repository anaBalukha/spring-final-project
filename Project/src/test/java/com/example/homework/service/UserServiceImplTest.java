package com.example.homework.service;

import com.example.homework.dto.request.RegisterRequest;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}, verifying password encoding,
 * default role assignment and duplicate-username handling.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register encodes the password and defaults the role to USER")
    void register_defaultsRoleToUser() {
        RegisterRequest request = new RegisterRequest("john", "secret123", null);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.register(request);

        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    @DisplayName("register honours an explicitly requested ADMIN role")
    void register_keepsRequestedRole() {
        RegisterRequest request = new RegisterRequest("boss", "secret123", Role.ADMIN);
        when(userRepository.existsByUsername("boss")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("register rejects a username that already exists")
    void register_duplicateUsername_throws() {
        RegisterRequest request = new RegisterRequest("taken", "secret123", null);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsername throws when the user is unknown")
    void findByUsername_notFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findByUsername returns the mapped profile when present")
    void findByUsername_found() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(
                User.builder().id(1L).username("admin").password("x").role(Role.ADMIN).build()));

        UserResponse response = userService.findByUsername("admin");

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getUsername()).isEqualTo("admin");
    }
}
