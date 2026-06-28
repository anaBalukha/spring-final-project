package com.example.homework.service;

import com.example.homework.dto.request.CourseRequest;
import com.example.homework.dto.response.CourseResponse;
import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CourseRequest request);
    List<CourseResponse> getAllCourses();
    CourseResponse getCourseById(Long id);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
    List<CourseResponse> getCoursesByStudentId(Long studentId);
}
