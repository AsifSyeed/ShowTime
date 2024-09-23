package com.example.showtime.user.service;

import com.example.showtime.auth.model.response.AuthResponse;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.model.request.ChangePasswordRequest;
import com.example.showtime.user.model.request.ForgetPasswordRequest;
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

    AuthResponse verifyUser(SignUpTfaVerifyRequest signUpTfaVerifyRequest);

    SignUpResponse sendGenericOtp(String emailId, int featureCode);

    void verifyForgetPassword(ForgetPasswordRequest forgetPasswordRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);
}
