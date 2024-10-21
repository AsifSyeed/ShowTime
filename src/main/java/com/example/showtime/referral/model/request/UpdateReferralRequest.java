package com.example.showtime.referral.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReferralRequest {
    private Long id;
    private String referralCode;
    private Double referralDiscount;
    private String eventId;
    private Integer referralType;
    private Boolean isActive;
}
