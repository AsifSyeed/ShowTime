package com.example.showtime.user.service.imp;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.tfa.enums.FeatureEnum;
import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import com.example.showtime.tfa.service.ITFAService;
import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.model.entity.UserAccount;
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
                    .sessionId(getTfaSessionId(userAccount.getEmail(), FeatureEnum.SIGN_UP.getValue()))
                    .build();
        } else {
            return SignUpResponse.builder()
                    .sessionId(getTfaSessionId(existingUserAccount.getEmail(), FeatureEnum.SIGN_UP.getValue()))
                    .build();
        }
    }

    private String getTfaSessionId(String email, int featureCode) {
        return tfaService.generateTfaSessionId(email, featureCode);
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
                    .userFullName(userAccount.getUserFullName())
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
        userRepository.save(userAccount);
    }

    private Boolean isOtpVerified(String email, TFAVerifyRequest tfaData) {
        return tfaService.verifyOtp(email, tfaData);
    }

    @Override
    public SignUpResponse forgetPassword(String emailId) {
        UserAccount userAccount = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

        return SignUpResponse.builder()
                .sessionId(getTfaSessionId(userAccount.getEmail(), FeatureEnum.FORGET_PASSWORD.getValue()))
                .build();
    }

    @Override
    public void verifyForgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        UserAccount userAccount = userRepository.findByEmail(forgetPasswordRequest.getEmail())
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

        if (isOtpVerified(forgetPasswordRequest.getEmail(), forgetPasswordRequest.getTfaData())) {
            userAccount.setPassword(passwordEncoder.encode(forgetPasswordRequest.getNewPassword()));
            userRepository.save(userAccount);
        }
    }

    private void validateRequest(SignUpRequest signUpRequest) {
        if (Objects.isNull(signUpRequest) ||
                StringUtils.isEmpty(signUpRequest.getEmail()) ||
                StringUtils.isEmpty(signUpRequest.getPhoneNumber()) ||
                StringUtils.isEmpty(signUpRequest.getPassword()) ||
                StringUtils.isEmpty(signUpRequest.getUserName()) ||
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
        userAccount.setUserFullName(signUpRequest.getUserFullName());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userAccount.setIsOtpVerified(false);

        return userAccount;
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
