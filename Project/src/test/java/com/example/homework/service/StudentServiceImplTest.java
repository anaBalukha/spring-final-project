package com.example.homework.service;

import com.example.homework.config.AppSettingsProperties;
import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.StudentResponse;
import com.example.homework.entity.Student;
import com.example.homework.exception.DuplicateResourceException;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.StudentRepository;
import com.example.homework.service.impl.StudentServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentServiceImpl} using Mockito to isolate the
 * service from the persistence layer. Covers both positive and negative paths.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AppSettingsProperties appSettings;

    private MeterRegistry meterRegistry;
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        // A real in-memory registry so the creation counter behaves naturally
        meterRegistry = new SimpleMeterRegistry();
        studentService = new StudentServiceImpl(studentRepository, appSettings, meterRegistry);
    }

    private Student student(Long id, String email) {
        return Student.builder()
                .id(id).firstName("Nino").lastName("Beridze").email(email)
                .build();
    }

    @Test
    @DisplayName("createStudent persists a new student and increments the metric")
    void createStudent_success() {
        StudentRequest request = new StudentRequest("Nino", "Beridze", "nino@example.com");
        when(studentRepository.existsByEmail("nino@example.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student(1L, "nino@example.com"));

        StudentResponse response = studentService.createStudent(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("nino@example.com");
        assertThat(meterRegistry.counter("app.students.created").count()).isEqualTo(1.0);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("createStudent rejects a duplicate email")
    void createStudent_duplicateEmail_throws() {
        StudentRequest request = new StudentRequest("Nino", "Beridze", "dupe@example.com");
        when(studentRepository.existsByEmail("dupe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(studentRepository, never()).save(any());
        assertThat(meterRegistry.counter("app.students.created").count()).isZero();
    }

    @Test
    @DisplayName("getStudentById returns the mapped response when found")
    void getStudentById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "a@example.com")));

        StudentResponse response = studentService.getStudentById(1L);

        assertThat(response.getEmail()).isEqualTo("a@example.com");
    }

    @Test
    @DisplayName("getStudentById throws when the student is missing")
    void getStudentById_notFound_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest(name = "limit={0} over {1} students returns {2}")
    @CsvSource({"5, 3, 3", "2, 5, 2", "10, 10, 10"})
    @DisplayName("getAllStudents honours the profile pagination limit")
    void getAllStudents_appliesPaginationLimit(int limit, int available, int expected) {
        when(appSettings.getPaginationLimit()).thenReturn(limit);
        List<Student> students = IntStream.rangeClosed(1, available)
                .mapToObj(i -> student((long) i, "s" + i + "@example.com"))
                .toList();
        when(studentRepository.findAll()).thenReturn(students);

        List<StudentResponse> result = studentService.getAllStudents();

        assertThat(result).hasSize(expected);
    }

    @Test
    @DisplayName("updateStudent rejects switching to an email owned by someone else")
    void updateStudent_emailTaken_throws() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "old@example.com")));
        when(studentRepository.existsByEmail("taken@example.com")).thenReturn(true);
        StudentRequest request = new StudentRequest("Nino", "Beridze", "taken@example.com");

        assertThatThrownBy(() -> studentService.updateStudent(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("deleteStudent removes an existing student")
    void deleteStudent_success() {
        Student existing = student(1L, "a@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));

        studentService.deleteStudent(1L);

        verify(studentRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteStudent throws when the student does not exist")
    void deleteStudent_notFound_throws() {
        when(studentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent(42L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(studentRepository, never()).delete(any());
    }
}
