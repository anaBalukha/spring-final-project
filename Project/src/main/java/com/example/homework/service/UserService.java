package com.example.homework.service;

import com.example.homework.dto.request.RegisterRequest;
import com.example.homework.dto.request.UserUpdateRequest;
import com.example.homework.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse findByUsername(String username);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
