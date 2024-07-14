package com.example.showtime.user.controller;

import com.example.showtime.user.model.request.ChangePasswordRequest;
import com.example.showtime.user.model.request.ForgetPasswordRequest;
import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.user.model.request.SignUpTfaVerifyRequest;
import com.example.showtime.user.model.response.SignUpResponse;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final IUserService iUserService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUpUser(@RequestBody @Valid SignUpRequest signUpRequest) {

        SignUpResponse signUpResponse = iUserService.signUpUser(signUpRequest);

        ApiResponse<SignUpResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Operation Successful", signUpResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile() {

        UserProfileResponse userProfileResponse = iUserService.getUserProfile();

        ApiResponse<UserProfileResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "User profile fetched successfully", userProfileResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyUser(@RequestBody @Valid SignUpTfaVerifyRequest signUpTfaVerifyRequest) {
        iUserService.verifyUser(signUpTfaVerifyRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "User verified successfully", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generic-otp")
    public ResponseEntity<ApiResponse<SignUpResponse>> sendGenericOtp(@RequestParam String emailId, int featureCode) {
        SignUpResponse signUpResponse = iUserService.sendGenericOtp(emailId, featureCode);

        ApiResponse<SignUpResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "OTP sent successfully", signUpResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forget-password/verify")
    public ResponseEntity<ApiResponse<?>> verifyForgetPassword(@RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        iUserService.verifyForgetPassword(forgetPasswordRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Password updated successfully", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        iUserService.changePassword(changePasswordRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Password updated successfully", null);

        return ResponseEntity.ok(response);
    }
}
