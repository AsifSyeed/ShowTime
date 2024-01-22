package com.example.showtime.admin.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminSignUpRequest {
    private String email;
    private String phoneNumber;
    private String password;
    private String userName;
}
