package com.example.homework.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{validation.user.username.required}")
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    private String password;
}
