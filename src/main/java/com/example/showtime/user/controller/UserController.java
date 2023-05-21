package com.example.showtime.user.controller;

import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.user.service.IUserService;
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
@RequestMapping("/api/v1/user")
public class UserController {

    private final IUserService iUserService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUpUser(@RequestBody @Valid SignUpRequest signUpRequest) {

        iUserService.signUpUser(signUpRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "User signed up successfully", null);

        return ResponseEntity.ok(response);
    }
}
