package com.example.homework.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<CourseResponse> courses;
}
