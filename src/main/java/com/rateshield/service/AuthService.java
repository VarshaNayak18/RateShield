package com.rateshield.service;

import com.rateshield.dto.LoginRequest;
import com.rateshield.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}