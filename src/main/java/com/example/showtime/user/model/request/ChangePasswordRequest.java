package com.example.showtime.user.model.request;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private TFAVerifyRequest tfaData;
}
