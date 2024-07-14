package com.example.showtime.tfa.service;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;

public interface ITFAService {
    String generateTfaSessionId(String userName, String email, int featureCode);

    Boolean verifyOtp(String email, TFAVerifyRequest tfaVerifyRequest);
}
