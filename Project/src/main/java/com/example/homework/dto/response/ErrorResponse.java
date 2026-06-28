package com.example.homework.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
}
