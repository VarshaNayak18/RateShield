package com.rateshield.service;

import com.rateshield.dto.UserRequest;
import com.rateshield.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);
}