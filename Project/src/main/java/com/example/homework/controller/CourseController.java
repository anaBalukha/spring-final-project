package com.example.homework.controller;

import com.example.homework.dto.request.CourseRequest;
import com.example.homework.dto.response.CourseResponse;
import com.example.homework.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "CRUD operations for managing courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Create a new course and assign it to a student — requires authentication")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by ID")
    public ResponseEntity<CourseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all courses belonging to a specific student")
    public ResponseEntity<List<CourseResponse>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getCoursesByStudentId(studentId));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Update a course by ID")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH_SCHEME)
    @Operation(summary = "Delete a course by ID — ADMIN only")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
