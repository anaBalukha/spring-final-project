package com.example.homework.service;

import com.example.homework.dto.request.CourseRequest;
import com.example.homework.dto.response.CourseResponse;
import com.example.homework.entity.Course;
import com.example.homework.entity.Student;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.CourseRepository;
import com.example.homework.repository.StudentRepository;
import com.example.homework.service.impl.CourseServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CourseServiceImpl}. The course always belongs to a
 * student, so the negative paths focus on the missing-student case.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudentRepository studentRepository;

    private MeterRegistry meterRegistry;
    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        courseService = new CourseServiceImpl(courseRepository, studentRepository, meterRegistry);
    }

    private Student student() {
        return Student.builder().id(1L).firstName("Ana").lastName("Lomidze").email("ana@example.com").build();
    }

    @Test
    @DisplayName("createCourse assigns the course to an existing student and counts it")
    void createCourse_success() {
        Student student = student();
        CourseRequest request = new CourseRequest("Spring Boot", "REST APIs", 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.save(any(Course.class))).thenReturn(
                Course.builder().id(7L).name("Spring Boot").description("REST APIs").student(student).build());

        CourseResponse response = courseService.createCourse(request);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getStudentId()).isEqualTo(1L);
        assertThat(meterRegistry.counter("app.courses.created").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("createCourse throws when the target student does not exist")
    void createCourse_studentMissing_throws() {
        CourseRequest request = new CourseRequest("Spring Boot", "REST APIs", 404L);
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCourseById throws when the course is missing")
    void getCourseById_notFound_throws() {
        when(courseRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCoursesByStudentId throws when the student is unknown")
    void getCoursesByStudentId_unknownStudent_throws() {
        when(studentRepository.existsById(8L)).thenReturn(false);

        assertThatThrownBy(() -> courseService.getCoursesByStudentId(8L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCoursesByStudentId returns the student's courses")
    void getCoursesByStudentId_success() {
        Student student = student();
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findByStudentId(1L)).thenReturn(List.of(
                Course.builder().id(1L).name("DB").student(student).build(),
                Course.builder().id(2L).name("Security").student(student).build()));

        List<CourseResponse> result = courseService.getCoursesByStudentId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteCourse removes an existing course")
    void deleteCourse_success() {
        Course course = Course.builder().id(3L).name("DB").student(student()).build();
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(3L);

        verify(courseRepository).delete(course);
    }
}
