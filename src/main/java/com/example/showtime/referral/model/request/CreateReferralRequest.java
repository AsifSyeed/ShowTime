package com.example.showtime.referral.model.request;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReferralRequest {
    private String referralCode;
    private Double referralDiscount;
    private String eventId;
    private Integer referralType;
}
