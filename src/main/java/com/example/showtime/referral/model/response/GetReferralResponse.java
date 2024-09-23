package com.example.showtime.referral.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetReferralResponse {
    private String referralCode;
    private Double referralDiscount;
    private String eventId;
    private Integer referralType;
}
