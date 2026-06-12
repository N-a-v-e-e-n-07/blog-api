package com.blogapi.service;

import com.blogapi.dto.request.LoginRequest;
import com.blogapi.dto.request.RegisterRequest;
import com.blogapi.dto.response.AuthResponse;
import com.blogapi.dto.response.UserSummary;

public interface AuthService {
    UserSummary register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
