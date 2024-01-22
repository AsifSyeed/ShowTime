package com.example.showtime.admin.service.imp;

import com.example.showtime.admin.model.entity.Admin;
import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.admin.service.IAdminService;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void signUpAdmin(AdminSignUpRequest adminSignUpRequest) {
        validateRequest(adminSignUpRequest);

        Admin admin = prepareAdminModel(adminSignUpRequest);

        adminRepository.save(admin);
    }

    private Admin prepareAdminModel(AdminSignUpRequest adminSignUpRequest) {
        Admin admin = new Admin();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        Admin createdBy = adminRepository.findByEmail(createdByUserEmail)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Admin not found"));

        admin.setEmail(adminSignUpRequest.getEmail());
        admin.setPhoneNumber(adminSignUpRequest.getPhoneNumber());
        admin.setAdminName(adminSignUpRequest.getUserName());
        admin.setPassword(passwordEncoder.encode(adminSignUpRequest.getPassword()));
        admin.setRole(createdBy.getRole());
        admin.setCreatedBy(createdBy.getAdminName());
        return admin;
    }

    private void validateRequest(AdminSignUpRequest adminSignUpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create an admin");
        }

        if (Objects.isNull(adminSignUpRequest) ||
                StringUtils.isEmpty(adminSignUpRequest.getEmail()) ||
                StringUtils.isEmpty(adminSignUpRequest.getPhoneNumber()) ||
                StringUtils.isEmpty(adminSignUpRequest.getPassword()) ||
                StringUtils.isEmpty(adminSignUpRequest.getUserName())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        if (isAdminEmailExists(adminSignUpRequest.getEmail())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Email ID already used");
        }

        if (isUserNameExists(adminSignUpRequest.getUserName())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Username already used");
        }

        if (isPhoneNumberExists(adminSignUpRequest.getPhoneNumber())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Phone Number already used");
        }
    }

    private boolean isAdminEmailExists(String email) {
        return adminRepository.existsByEmail(email);
    }

    private boolean isUserNameExists(String userName) {
        return adminRepository.existsAdminByAdminName(userName);
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        return adminRepository.existsByPhoneNumber(phoneNumber);
    }
}
