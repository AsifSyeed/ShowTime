package com.example.showtime.admin.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserInfoResponse {
    private String userName;
    private String emailId;
    private String phoneNumber;
    private String userFirstName;
    private String userLastName;
    private int userRole;
    private Long numberOfTickets;
}
