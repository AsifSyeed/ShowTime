package com.example.showtime.user.model.request;

import lombok.*;

import javax.validation.constraints.Email;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SignUpRequest {

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String password;
    private int userRole;
    private String userName;
    private String firstName;
    private String lastName;
    private String referredBy;
}
