package com.example.homework.controller;

import com.example.homework.dto.request.LoginRequest;
import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.response.UserResponse;
import com.example.homework.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, logout, and user management")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;

    @PostMapping("/register")
    @Operation(summary = "Register a new user — omit 'role' to default to USER; set it to ADMIN to create an admin")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Validate credentials and return the user profile (alternatively use HTTP Basic auth header on any request)")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("User {} logged in successfully", request.getUsername());
        return ResponseEntity.ok(userService.findByUsername(request.getUsername()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — clears the security context; discard your credentials on the client side. Response message is localized")
    public ResponseEntity<String> logout(Authentication authentication) {
        SecurityContextHolder.clearContext();
        if (authentication != null) {
            log.info("User {} logged out", authentication.getName());
        }
        return ResponseEntity.ok(messageSource.getMessage(
                "auth.logout.success", null, LocaleContextHolder.getLocale()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the currently authenticated user's profile — requires authentication")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findByUsername(authentication.getName()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all registered users — ADMIN only")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
