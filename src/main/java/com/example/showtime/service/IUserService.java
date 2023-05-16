package com.example.showtime.service;

import com.example.showtime.model.request.SignUpRequest;
import com.example.showtime.model.response.SignUpResponse;

public interface IUserService {
    SignUpResponse signUpUser(SignUpRequest signUpRequest);

}
