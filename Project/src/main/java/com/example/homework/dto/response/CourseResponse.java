package com.example.homework.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private Long studentId;
}
