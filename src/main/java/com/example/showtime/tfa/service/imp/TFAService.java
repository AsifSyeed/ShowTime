package com.example.showtime.tfa.service.imp;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.uniqueId.UniqueIdGenerator;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.tfa.enums.FeatureEnum;
import com.example.showtime.tfa.model.entity.GeneratedOtp;
import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import com.example.showtime.tfa.repository.TFARepository;
import com.example.showtime.tfa.service.ITFAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TFAService implements ITFAService {
    private final TFARepository tfaRepository;
    private final UniqueIdGenerator uniqueIdGenerator;
    private final IEmailService emailService;

    @Override
    public String generateTfaSessionId(String userName, String email, int featureCode) {
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

        String htmlContent = String.format(
                "<p style=\"font-size: 16px;\"><strong>Dear %s</strong></p>"
                        + "<p>Your One Time Password is: <strong>%s</strong></p>"
                        + "<p>Regards,<br/>Relevant Bangladesh</p>",
                userName, generatedOtp.getOtp()
        );

        String subject = "OTP for " + FeatureEnum.fromValue(featureCode).getDescription();

        CompletableFuture.runAsync(() -> sendOtpEmail(email, subject, htmlContent));
        return generatedOtp.getSessionId();
    }

    private void sendOtpEmail(String email, String subject, String htmlContent) {
        emailService.sendGenericEmail(email, subject, htmlContent);
    }

    @Override
    public Boolean verifyOtp(String email, TFAVerifyRequest tfaVerifyRequest) {
        GeneratedOtp generatedOtp = tfaRepository.findByCreatedByAndSessionIdAndOtpAndFeatureCode(email, tfaVerifyRequest.getSessionId(), tfaVerifyRequest.getOtp(), tfaVerifyRequest.getFeatureCode());

        if (generatedOtp == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "OTP not found");
        }

        if (!generatedOtp.getOtp().equals(tfaVerifyRequest.getOtp())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid OTP");
        }

        //Check if used
        if (generatedOtp.getIsUsed()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "OTP is used");
        }

        if (Calendar.getInstance().getTime().after(generatedOtp.getExpireAt())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "OTP expired");
        }

        generatedOtp.setIsUsed(true);
        tfaRepository.save(generatedOtp);

        return true;
    }
}
