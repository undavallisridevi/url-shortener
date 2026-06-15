package com.sridevi.urlshortener.service;

import com.sridevi.urlshortener.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
