package com.example.showtime.service.imp;

import com.example.showtime.enums.UserRole;
import com.example.showtime.model.entity.User;
import com.example.showtime.model.request.SignUpRequest;
import com.example.showtime.model.response.SignUpResponse;
import com.example.showtime.repository.UserRepository;
import com.example.showtime.service.IUserService;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) {

        validateRequest(signUpRequest);

        User user = prepareUserModel(signUpRequest);
        User savedUser = userRepository.save(user);
        Long generatedId = savedUser.getId();

        // Generate the user ID based on the generated ID
        String userId = generateUserId(signUpRequest, generatedId);
        savedUser.setUserId(userId);

        // Update the user entity with the generated user ID and save it again
        userRepository.save(savedUser);

        return SignUpResponse.builder()
                .userName(savedUser.getUserName())
                .emailId(savedUser.getEmail())
                .build();
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

    private User prepareUserModel(SignUpRequest signUpRequest) {
        User user = new User();

        user.setUserCreationDate(new Date());
        user.setUserName(signUpRequest.getUserName());
        user.setEmail(signUpRequest.getEmail());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setRole(signUpRequest.getUserRole());

        return user;
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

    private String generateUserId(SignUpRequest signUpRequest, Long id) {
        StringBuilder sb = new StringBuilder();

        // Get the first character of the user role and make it uppercase
        String[] words = signUpRequest.getUserName().split("\\s+");

        // Iterate over each word and append its capitalized first letter to the random string
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        // Get the long ID as a string
        String idString = String.valueOf(id);
        sb.append(idString);

        // Get the last 3 digits of the phone number
        String lastDigits = signUpRequest.getPhoneNumber().substring(signUpRequest.getPhoneNumber().length() - 3);
        sb.append(lastDigits);

        // Pad the resulting string with zeroes or truncate if necessary
        String userId = sb.toString();
        if (userId.length() < 8) {
            userId = userId + "00000000".substring(userId.length());
        } else if (userId.length() > 8) {
            userId = userId.substring(0, 8);
        }

        return userId;
    }
}
