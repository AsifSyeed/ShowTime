package com.example.showtime.user.service.imp;

import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.model.request.SignUpRequest;
import com.example.showtime.user.repository.UserRepository;
import com.example.showtime.user.service.IUserService;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void signUpUser(SignUpRequest signUpRequest) {

        validateRequest(signUpRequest);

        UserAccount userAccount = prepareUserModel(signUpRequest);

        userRepository.save(userAccount);
    }

    private void validateRequest(SignUpRequest signUpRequest) {
        if (Objects.isNull(signUpRequest) ||
                StringUtils.isEmpty(signUpRequest.getEmail()) ||
                StringUtils.isEmpty(signUpRequest.getPhoneNumber()) ||
                StringUtils.isEmpty(signUpRequest.getPassword()) ||
                StringUtils.isEmpty(signUpRequest.getUserName()) ||
                !isValidUserRole(signUpRequest.getUserRole())) {

            throw new InvalidRequestStateException("Request body is not valid");
        }

        if (isEmailExists(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Email ID already used");
        }

        if (isUserNameExists(signUpRequest.getUserName())) {
            throw new IllegalArgumentException("Username already used");
        }

        if (isPhoneNumberExists(signUpRequest.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone Number already used");
        }
    }

    private UserAccount prepareUserModel(SignUpRequest signUpRequest) {
        UserAccount userAccount = new UserAccount();

        userAccount.setUserCreationDate(new Date());
        userAccount.setUserName(signUpRequest.getUserName());
        userAccount.setEmail(signUpRequest.getEmail());
        userAccount.setPhoneNumber(signUpRequest.getPhoneNumber());
        userAccount.setRole(signUpRequest.getUserRole());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

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
