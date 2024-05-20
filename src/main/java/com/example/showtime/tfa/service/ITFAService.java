package com.example.showtime.tfa.service;

public interface ITFAService {
    String generateTfaSessionId(String email, int featureCode);

    Boolean verifyOtp(String email, String sessionId, String otp, int featureCode);
}
