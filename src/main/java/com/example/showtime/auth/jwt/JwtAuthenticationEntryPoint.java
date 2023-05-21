package com.example.showtime.auth.jwt;

import com.example.showtime.common.model.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ApiResponse<?> errorResponse = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);

        // Set the response status code
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Set the response content type
        response.setContentType("application/json");

        // Write the error response to the response body
        ObjectMapper objectMapper = new ObjectMapper();
        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, errorResponse);
        outputStream.flush();
    }
}
