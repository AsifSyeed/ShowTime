package com.example.showtime.user.service;

import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.UserProfileResponse;

import java.util.List;

public interface IUserService {
    void signUpUser(SignUpRequest signUpRequest);

    UserProfileResponse getUserProfile();

    List<UserAccount> getUserList();

    UserAccount getUserByEmail(String email);
}
