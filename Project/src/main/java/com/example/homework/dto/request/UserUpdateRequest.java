package com.example.homework.dto.request;

import com.example.homework.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "{validation.user.username.required}")
    @Size(min = 3, max = 50, message = "{validation.user.username.size}")
    private String username;

    /*
     * Password is optional during update. Send null when the password should remain unchanged.
     */
    @Size(min = 6, message = "{validation.user.password.size}")
    private String password;

    @NotNull(message = "{validation.user.role.required}")
    private Role role;
}