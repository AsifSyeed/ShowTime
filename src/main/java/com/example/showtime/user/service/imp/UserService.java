package com.example.showtime.user.service.imp;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.tfa.enums.FeatureEnum;
import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import com.example.showtime.tfa.service.ITFAService;
import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.model.request.ChangePasswordRequest;
import com.example.showtime.user.model.request.ForgetPasswordRequest;
import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.user.model.request.SignUpTfaVerifyRequest;
import com.example.showtime.user.model.response.SignUpResponse;
import com.example.showtime.user.repository.UserRepository;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ITFAService tfaService;
    private final IEmailService emailService;

    @Override
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) {

        validateRequest(signUpRequest);

        UserAccount existingUserAccount = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);

        if (Objects.nonNull(existingUserAccount) && existingUserAccount.getIsOtpVerified()) {
            if (isEmailExists(signUpRequest.getEmail())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Email already used");
            }

            if (isUserNameExists(signUpRequest.getUserName())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Username already used");
            }

            if (isPhoneNumberExists(signUpRequest.getPhoneNumber())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Phone Number already used");
            }
        }

        if (existingUserAccount == null) {
            UserAccount userAccount = prepareUserModel(signUpRequest);

            userRepository.save(userAccount);

            return SignUpResponse.builder()
                    .sessionId(getTfaSessionId(userAccount.getFirstName() + " " + userAccount.getLastName(), userAccount.getEmail(), FeatureEnum.SIGN_UP.getValue()))
                    .build();
        } else {
            return SignUpResponse.builder()
                    .sessionId(getTfaSessionId(existingUserAccount.getFirstName() + " " + existingUserAccount.getLastName(), existingUserAccount.getEmail(), FeatureEnum.SIGN_UP.getValue()))
                    .build();
        }
    }

    private String getTfaSessionId(String userName, String email, int featureCode) {
        return tfaService.generateTfaSessionId(userName, email, featureCode);
    }

    @Override
    public UserProfileResponse getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String createdByUserEmail = authentication.getName();

            UserAccount userAccount = userRepository.findByEmail(createdByUserEmail)
                    .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

            return UserProfileResponse.builder()
                    .userName(userAccount.getUserName())
                    .emailId(userAccount.getEmail())
                    .phoneNumber(userAccount.getPhoneNumber())
                    .userFullName(userAccount.getLastName())
                    .userRole(userAccount.getRole())
                    .build();
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public List<UserAccount> getUserList() {
        return userRepository.findAll();
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));
    }

    @Override
    public void verifyUser(SignUpTfaVerifyRequest signUpTfaVerifyRequest) {
        UserAccount userAccount = userRepository.findByEmail(signUpTfaVerifyRequest.getEmail())
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

        userAccount.setIsOtpVerified(isOtpVerified(signUpTfaVerifyRequest.getEmail(), signUpTfaVerifyRequest.getTfaData()));
        String htmlContent = String.format(
                "<p style=\"font-size: 16px;\">Dear <strong>%s</strong>,</p>"
                        + "<p>Thank you for confirming your account! Your registration is now complete, and you can start enjoying all the benefits of being a member of countersbd.com.</p>"
                        + "<p>Here’s what you can do next:</p>"
                        + "<ul>"
                        + "  <li><a href=\"https://www.countersbd.com/auth/signin\">Log in to Your Account</a></li>"
                        + "  <li><a href=\"https://www.countersbd.com/events\">Explore Our Events</a></li>"
                        + "  <li><a href=\"https://www.countersbd.com/aboutus\">About Us</a></li>"
                        + "</ul>"
                        + "<p>If you have any questions or need assistance, our support team is here to help. Feel free to reach out to us at <a href=\"mailto:releventbangladesh@gmail.com\">releventbangladesh@gmail.com</a>.</p>"
                        + "<p>Welcome once again, and we’re thrilled to have you with us!</p>"
                        + "<p>Regards,</p>"
                        + "<p>Relevent Bangladesh</p>",
                userAccount.getFirstName() + " " + userAccount.getLastName()
        );

        userRepository.save(userAccount);
        sendEmail(userAccount.getEmail(), "Welcome to Counters BD", htmlContent);
    }

    private Boolean isOtpVerified(String email, TFAVerifyRequest tfaData) {
        return tfaService.verifyOtp(email, tfaData);
    }

    private void sendEmail(String emailId, String subject, String htmlContent) {
        emailService.sendGenericEmail(emailId, subject, htmlContent);
    }

    @Override
    public SignUpResponse sendGenericOtp(String emailId, int featureCode) {
        if (featureCode == 0 || emailId == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid request");
        }

        if (featureCode == FeatureEnum.FORGET_PASSWORD.getValue()) {
            UserAccount userAccount = userRepository.findByEmail(emailId)
                    .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

            return SignUpResponse.builder()
                    .sessionId(getTfaSessionId(userAccount.getFirstName() + " " + userAccount.getLastName(), userAccount.getEmail(), featureCode))
                    .build();
        } else {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String createdByUserEmail = authentication.getName();

                UserAccount userAccount = userRepository.findByEmail(createdByUserEmail)
                        .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

                if (userAccount.getEmail().equals(emailId)) {
                    return SignUpResponse.builder()
                            .sessionId(getTfaSessionId(userAccount.getFirstName() + " " + userAccount.getLastName(), userAccount.getEmail(), featureCode))
                            .build();
                } else {
                    throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid request");
                }
            } catch (AccessDeniedException e) {
                throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
            }
        }
    }

    @Override
    public void verifyForgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        UserAccount userAccount = userRepository.findByEmail(forgetPasswordRequest.getEmail())
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

        if (isOtpVerified(forgetPasswordRequest.getEmail(), forgetPasswordRequest.getTfaData())) {
            userAccount.setPassword(passwordEncoder.encode(forgetPasswordRequest.getNewPassword()));
            userRepository.save(userAccount);

            String htmlContent = String.format(
                    "<p style=\"font-size: 16px;\">Dear <strong>%s</strong>,</p>"
                            + "<p>Your password has been updated. If you have not performed this action, please contact us at <a href=\"mailto:releventbangladesh@gmail.com\">releventbangladesh@gmail.com</a>.</p>"
                            + "<p>Regards,</p>"
                            + "<p>Relevent Bangladesh</p>",
                    userAccount.getFirstName() + " " + userAccount.getLastName()
            );

            sendEmail(userAccount.getEmail(), "Password Updated", htmlContent);
        }
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            UserAccount userAccount = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

            if (isOtpVerified(userAccount.getEmail(), changePasswordRequest.getTfaData())) {
                if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), userAccount.getPassword())) {
                    userAccount.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                    userRepository.save(userAccount);
                } else {
                    throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid old password");
                }
            }
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private void validateRequest(SignUpRequest signUpRequest) {
        if (Objects.isNull(signUpRequest) ||
                StringUtils.isEmpty(signUpRequest.getEmail()) ||
                StringUtils.isEmpty(signUpRequest.getPhoneNumber()) ||
                StringUtils.isEmpty(signUpRequest.getPassword()) ||
                StringUtils.isEmpty(signUpRequest.getUserName()) ||
                StringUtils.isEmpty(signUpRequest.getFirstName()) ||
                StringUtils.isEmpty(signUpRequest.getLastName()) ||
                !isValidUserRole(signUpRequest.getUserRole())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }
    }

    private UserAccount prepareUserModel(SignUpRequest signUpRequest) {
        UserAccount userAccount = new UserAccount();

        userAccount.setUserCreationDate(new Date());
        userAccount.setUserName(signUpRequest.getUserName());
        userAccount.setEmail(signUpRequest.getEmail());
        userAccount.setPhoneNumber(signUpRequest.getPhoneNumber());
        userAccount.setRole(signUpRequest.getUserRole());
        userAccount.setFirstName(signUpRequest.getFirstName());
        userAccount.setLastName(signUpRequest.getLastName());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userAccount.setIsOtpVerified(false);
        userAccount.setReferredBy(signUpRequest.getReferredBy());
        userAccount.setReferralCode(generateReferralCode(userAccount));

        return userAccount;
    }

    private String generateReferralCode(UserAccount userAccount) {
        // Generate referral code with the first 2 characters of the first name and the last 2 characters of the last name and 3 random numbers
        return userAccount.getFirstName().substring(0, 2) + userAccount.getLastName().substring(userAccount.getLastName().length() - 2) + (int) (Math.random() * 900 + 100);
    }

    private boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private boolean isUserNameExists(String userName) {
        return userRepository.existsByUserName(userName);
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    private boolean isValidUserRole(int userRole) {
        for (UserRole role : UserRole.values()) {
            if (role.getValue() == userRole) {
                return true;
            }
        }
        return false;
    }
}
