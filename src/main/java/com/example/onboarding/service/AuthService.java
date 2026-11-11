package com.example.onboarding.service;

import com.example.onboarding.dto.request.LoginRequest;
import com.example.onboarding.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
