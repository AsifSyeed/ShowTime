package com.example.showtime.admin.service;

import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.common.model.response.UserProfileResponse;

import java.util.List;

public interface IAdminService {
    void signUpAdmin(AdminSignUpRequest adminSignUpRequest);

    UserProfileResponse getAdminProfile();

    List<UserProfileResponse> getUserList();
}
