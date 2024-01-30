package com.example.showtime.admin.controller;

import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.service.IAdminService;
import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.common.model.response.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final IAdminService iAdminService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUpAdmin(@RequestBody @Valid AdminSignUpRequest adminSignUpRequest) {

        iAdminService.signUpAdmin(adminSignUpRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Admin created successfully", null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile() {

        UserProfileResponse userProfileResponse = iAdminService.getAdminProfile();

        ApiResponse<UserProfileResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "User profile fetched successfully", userProfileResponse);

        return ResponseEntity.ok(response);
    }
}
