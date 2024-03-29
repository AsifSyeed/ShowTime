package com.example.showtime.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String userName;
    private String emailId;
    private String phoneNumber;
}
