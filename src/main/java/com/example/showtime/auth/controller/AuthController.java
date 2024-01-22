package com.example.showtime.auth.controller;

import com.example.showtime.auth.model.request.AuthRequest;
import com.example.showtime.auth.model.response.AuthResponse;
import com.example.showtime.auth.service.IAuthService;
import com.example.showtime.common.model.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IAuthService authService;
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid AuthRequest authRequest) {

        AuthResponse authResponse = authService.login(authRequest);

        ApiResponse<AuthResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "User logged in successfully!", authResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/token")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(@RequestBody @Valid AuthRequest authRequest) {

        AuthResponse authResponse = authService.adminLogin(authRequest);

        ApiResponse<AuthResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Admin logged in successfully!", authResponse);

        return ResponseEntity.ok(response);
    }

}
