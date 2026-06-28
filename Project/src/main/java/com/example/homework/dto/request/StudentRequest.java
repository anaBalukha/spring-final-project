package com.example.homework.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest {

    @NotBlank(message = "{validation.student.firstname.required}")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "{validation.student.lastname.required}")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "{validation.student.email.required}")
    @Email(message = "{validation.student.email.invalid}")
    private String email;
}
