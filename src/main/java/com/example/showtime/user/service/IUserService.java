package com.example.showtime.user.service;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.user.model.request.SignUpTfaVerifyRequest;
import com.example.showtime.user.model.response.SignUpResponse;

import java.util.List;

public interface IUserService {
    SignUpResponse signUpUser(SignUpRequest signUpRequest);

    UserProfileResponse getUserProfile();

    List<UserAccount> getUserList();

    UserAccount getUserByEmail(String email);

    void verifyUser(SignUpTfaVerifyRequest signUpTfaVerifyRequest);
}
