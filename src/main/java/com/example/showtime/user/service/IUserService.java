package com.example.showtime.user.service;

import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.UserProfileResponse;

public interface IUserService {
    void signUpUser(SignUpRequest signUpRequest);

    UserProfileResponse getUserProfile();
}
