package com.example.showtime.tfa.repository;

import com.example.showtime.tfa.model.entity.GeneratedOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TFARepository extends JpaRepository<GeneratedOtp, Long> {

    GeneratedOtp findByCreatedByAndSessionIdAndOtp(String email, String sessionId, String otp);
}
