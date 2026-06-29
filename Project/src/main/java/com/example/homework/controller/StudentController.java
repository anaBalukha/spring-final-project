package com.example.homework.controller;

import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.StudentResponse;
import com.example.homework.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.homework.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "CRUD operations for managing students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Create a new student — requires authentication")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping
    @Operation(summary = "Get all students with their enrolled courses")
    public ResponseEntity<List<StudentResponse>> getAll() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student by ID")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Update a student by ID")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Delete a student and all their courses — ADMIN only")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
