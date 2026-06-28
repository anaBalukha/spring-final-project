package com.example.homework.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "{validation.course.name.required}")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "{validation.course.studentid.required}")
    private Long studentId;
}
