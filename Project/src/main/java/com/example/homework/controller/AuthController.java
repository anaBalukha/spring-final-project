package com.example.homework.controller;

import com.example.homework.config.OpenApiConfig;
import com.example.homework.dto.request.LoginRequest;
import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.request.UserUpdateRequest;
import com.example.homework.dto.response.UserResponse;
import com.example.homework.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Registration, authentication and user management"
)
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new account — always creates a USER account"
    )
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Validate credentials and return the user profile"
    )
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        log.info(
                "User {} logged in successfully",
                request.getUsername()
        );

        return ResponseEntity.ok(
                userService.findByUsername(request.getUsername())
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")

    // Swagger knows this endpoint needs credentials.
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "Clear the current security context"
    )
    public ResponseEntity<String> logout(
            Authentication authentication
    ) {
        SecurityContextHolder.clearContext();

        if (authentication != null) {
            log.info(
                    "User {} logged out",
                    authentication.getName()
            );
        }

        return ResponseEntity.ok(
                messageSource.getMessage(
                        "auth.logout.success",
                        null,
                        LocaleContextHolder.getLocale()
                )
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "Get the authenticated user's profile"
    )
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userService.findByUsername(authentication.getName())
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "List all registered users — ADMIN only"
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "Get a registered user by ID — ADMIN only"
    )
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }


    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "Update a registered user — ADMIN only"
    )
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }


    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(
            name = OpenApiConfig.BASIC_AUTH_SCHEME
    )
    @Operation(
            summary = "Delete a registered user — ADMIN only"
    )
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}