package com.example.homework.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{validation.user.username.required}")
    @Size(min = 3, max = 50, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 6, message = "{validation.user.password.size}")
    private String password;

}
