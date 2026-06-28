package com.example.homework.dto.response;

import com.example.homework.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
}
