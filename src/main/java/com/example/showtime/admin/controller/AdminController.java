package com.example.showtime.admin.controller;

import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.service.IAdminService;
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
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final IAdminService iAdminService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUpAdmin(@RequestBody @Valid AdminSignUpRequest adminSignUpRequest) {

        iAdminService.signUpAdmin(adminSignUpRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Admin created successfully", null);

        return ResponseEntity.ok(response);
    }
}
