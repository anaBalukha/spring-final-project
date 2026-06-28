package com.example.homework.service;

import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.StudentResponse;
import java.util.List;

public interface StudentService {
    StudentResponse createStudent(StudentRequest request);
    List<StudentResponse> getAllStudents();
    StudentResponse getStudentById(Long id);
    StudentResponse updateStudent(Long id, StudentRequest request);
    void deleteStudent(Long id);
}
