package com.example.homework.service.impl;

import com.example.homework.dto.request.CourseRequest;
import com.example.homework.dto.response.CourseResponse;
import com.example.homework.entity.Course;
import com.example.homework.entity.Student;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.repository.CourseRepository;
import com.example.homework.repository.StudentRepository;
import com.example.homework.service.CourseService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public CourseResponse createCourse(CourseRequest request) {
        log.debug("Creating course '{}' for student {}", request.getName(), request.getStudentId());
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("error.student.notfound", request.getStudentId()));
        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .student(student)
                .build();
        Course saved = courseRepository.save(course);
        meterRegistry.counter("app.courses.created").increment();
        log.info("Course created: id={}, name={}, studentId={}", saved.getId(), saved.getName(), student.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        List<CourseResponse> courses = courseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.debug("Fetched {} course(s)", courses.size());
        return courses;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.debug("Fetching course by id {}", id);
        return toResponse(findById(id));
    }

    @Override
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = findById(id);
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("error.student.notfound", request.getStudentId()));
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setStudent(student);
        Course saved = courseRepository.save(course);
        log.info("Course updated: id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public void deleteCourse(Long id) {
        courseRepository.delete(findById(id));
        log.info("Course deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByStudentId(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("error.student.notfound", studentId);
        }
        return courseRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.course.notfound", id));
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .studentId(course.getStudent().getId())
                .build();
    }
}
