package com.example.showtime.user.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SignUpRequest {
    private String email;
    private String phoneNumber;
    private String password;
    private int userRole;
    private String userName;
    private String firstName;
    private String lastName;
}
