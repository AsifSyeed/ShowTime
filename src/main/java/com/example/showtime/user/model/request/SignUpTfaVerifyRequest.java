package com.example.showtime.user.model.request;

import com.example.showtime.tfa.model.request.TFAVerifyRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpTfaVerifyRequest {
    String email;
    TFAVerifyRequest tfaData;
}
