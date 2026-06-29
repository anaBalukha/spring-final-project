package com.example.homework.service.impl;

import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.CourseResponse;
import com.example.homework.dto.response.StudentResponse;
import com.example.homework.entity.Student;
import com.example.homework.exception.DuplicateResourceException;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.StudentRepository;
import com.example.homework.service.StudentService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    private final MeterRegistry meterRegistry;

    @Override
    public StudentResponse createStudent(
            StudentRequest request
    ) {
        log.debug(
                "Creating student with email {}",
                request.getEmail()
        );

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "error.student.email.exists",
                    request.getEmail()
            );
        }

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .build();

        Student saved = studentRepository.save(student);

        meterRegistry
                .counter("app.students.created")
                .increment();

        log.info(
                "Student created: id={}, email={}",
                saved.getId(),
                saved.getEmail()
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {

        // CHANGED:
        // No artificial limit. This returns every record.
        List<StudentResponse> students =
                studentRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        log.debug(
                "Fetched {} student(s)",
                students.size()
        );

        return students;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        log.debug("Fetching student by id {}", id);

        return toResponse(findById(id));
    }

    @Override
    public StudentResponse updateStudent(
            Long id,
            StudentRequest request
    ) {
        Student student = findById(id);

        boolean emailChanged =
                !student.getEmail().equals(request.getEmail());

        if (emailChanged
                && studentRepository.existsByEmail(
                request.getEmail()
        )) {
            throw new DuplicateResourceException(
                    "error.student.email.exists",
                    request.getEmail()
            );
        }

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());

        Student saved = studentRepository.save(student);

        log.info("Student updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = findById(id);

        studentRepository.delete(student);

        log.info("Student deleted: id={}", id);
    }

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "error.student.notfound",
                                id
                        )
                );
    }

    private StudentResponse toResponse(Student student) {
        List<CourseResponse> courses =
                student.getCourses()
                        .stream()
                        .map(course ->
                                CourseResponse.builder()
                                        .id(course.getId())
                                        .name(course.getName())
                                        .description(
                                                course.getDescription()
                                        )
                                        .studentId(student.getId())
                                        .build()
                        )
                        .toList();

        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .courses(courses)
                .build();
    }
}