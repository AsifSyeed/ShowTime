package com.example.showtime.auth.service.impl;

import com.example.showtime.auth.jwt.utlis.JwtUtil;
import com.example.showtime.auth.model.request.AuthRequest;
import com.example.showtime.auth.model.response.AuthResponse;
import com.example.showtime.auth.service.IAuthService;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.repository.UserRepository;
import com.example.showtime.user.service.imp.UserDetailsServiceImplementation;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsServiceImplementation userDetailsService;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest authRequest) {

        try {
            validateRequest(authRequest);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            String token = jwtUtil.generateToken(userDetails, authRequest.getUserRole());

            return AuthResponse.builder()
                    .token(token)
                    .build();

        } catch (AccessDeniedException e) {
            throw new IllegalArgumentException("Unauthorized", e);
        }
    }

    private void validateRequest(AuthRequest authRequest) {
        if (Objects.isNull(authRequest) ||
                StringUtils.isEmpty(authRequest.getEmail()) ||
                StringUtils.isEmpty(authRequest.getPassword())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Optional<UserAccount> userAccountOptional = userRepository.findByEmail(authRequest.getEmail());

        if (userAccountOptional.isEmpty()) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "User not found");
        }

        if (authRequest.getUserRole() != userAccountOptional.get().getRole()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User role is not valid");
        }
    }
}
