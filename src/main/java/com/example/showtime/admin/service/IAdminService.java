package com.example.showtime.admin.service;

import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.model.response.AdminUserInfoResponse;
import com.example.showtime.admin.model.response.DashboardInfoResponse;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.ticket.model.response.EventCategoryResponse;

import java.util.List;

public interface IAdminService {
    void signUpAdmin(AdminSignUpRequest adminSignUpRequest);

    UserProfileResponse getAdminProfile();

    List<AdminUserInfoResponse> getUserList();

    DashboardInfoResponse getDashboardInfo(String eventId);
}
