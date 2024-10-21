package com.example.showtime.admin.controller;

import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.model.response.DashboardInfoResponse;
import com.example.showtime.admin.service.IAdminService;
import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com", "https://admin.countersbd.com"}, maxAge = 3600)
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

    @GetMapping("/users/list")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getUserList() {

        List<UserProfileResponse> userProfileResponse = iAdminService.getUserList();

        ApiResponse<List<UserProfileResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "User list fetched successfully", userProfileResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-info")
    public ResponseEntity<ApiResponse<DashboardInfoResponse>> getDashboardInfo() {
        DashboardInfoResponse dashboardInfo = iAdminService.getDashboardInfo(null);
        ApiResponse<DashboardInfoResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Dashboard info fetched successfully", dashboardInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-info/{eventId}")
    public ResponseEntity<ApiResponse<DashboardInfoResponse>> getDashboardInfo(
            @PathVariable String eventId) {
        DashboardInfoResponse dashboardInfo = iAdminService.getDashboardInfo(eventId);
        ApiResponse<DashboardInfoResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Dashboard info fetched successfully", dashboardInfo);
        return ResponseEntity.ok(response);
    }
}
