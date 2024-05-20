package com.example.showtime.tfa.service.imp;

import com.example.showtime.common.uniqueId.UniqueIdGenerator;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.tfa.model.entity.GeneratedOtp;
import com.example.showtime.tfa.repository.TFARepository;
import com.example.showtime.tfa.service.ITFAService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@RequiredArgsConstructor
public class TFAService implements ITFAService {
    private final TFARepository tfaRepository;
    private final UniqueIdGenerator uniqueIdGenerator;
    private final IEmailService emailService;

    @Override
    public String generateTfaSessionId(String email, int featureCode) {
        GeneratedOtp generatedOtp = new GeneratedOtp();

        generatedOtp.setCreatedAt(Calendar.getInstance().getTime());
        //Set expiry time to 2 minutes
        Calendar expiryTime = Calendar.getInstance();
        expiryTime.add(Calendar.MINUTE, 2);
        generatedOtp.setExpireAt(expiryTime.getTime());
        generatedOtp.setSessionId(uniqueIdGenerator.generateUniqueUUID("su"));
        generatedOtp.setOtp(uniqueIdGenerator.generateOTP());
        generatedOtp.setIsUsed(false);
        generatedOtp.setCreatedBy(email);
        generatedOtp.setFeatureCode(featureCode);

        tfaRepository.save(generatedOtp);
        emailService.sendOtp(email, generatedOtp.getOtp());
        return generatedOtp.getSessionId();
    }

    @Override
    public Boolean verifyOtp(String email, String sessionId, String otp, int featureCode) {
        GeneratedOtp generatedOtp = tfaRepository.findByCreatedByAndSessionIdAndOtp(email, sessionId, otp);

        if (generatedOtp == null) {
            throw new RuntimeException("Invalid session id");
        }

        if (!generatedOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        //Check if used
        if (generatedOtp.getIsUsed()) {
            throw new RuntimeException("OTP already used");
        }

        if (Calendar.getInstance().getTime().after(generatedOtp.getExpireAt())) {
            throw new RuntimeException("OTP expired");
        }

        generatedOtp.setIsUsed(true);
        tfaRepository.save(generatedOtp);

        return true;
    }
}
