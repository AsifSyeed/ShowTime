package com.example.showtime.user.model.request;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgetPasswordRequest {
    String email;
    String userRole;
    String newPassword;
    TFAVerifyRequest tfaData;
}
