package com.example.showtime.tfa.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TFAVerifyRequest {
    private String sessionId;
    private int featureCode;
    private String otp;
}
