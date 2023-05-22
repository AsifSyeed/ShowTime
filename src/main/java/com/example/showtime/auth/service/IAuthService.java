package com.example.showtime.auth.service;

import com.example.showtime.auth.model.request.AuthRequest;
import com.example.showtime.auth.model.response.AuthResponse;

public interface IAuthService {
    AuthResponse login(AuthRequest authRequest);
}
