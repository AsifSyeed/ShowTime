package com.example.showtime.user.controller;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;
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

    @PostMapping("/forget-password/otp")
    public ResponseEntity<ApiResponse<SignUpResponse>> forgetPassword(@RequestParam String emailId) {
        SignUpResponse signUpResponse = iUserService.forgetPassword(emailId);

        ApiResponse<SignUpResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "OTP sent to your email", signUpResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forget-password/verify")
    public ResponseEntity<ApiResponse<?>> verifyForgetPassword(@RequestBody @Valid ForgetPasswordRequest forgetPasswordRequest) {
        iUserService.verifyForgetPassword(forgetPasswordRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Password updated successfully", null);

        return ResponseEntity.ok(response);
    }
}
