package com.example.showtime.referral.enums;

import lombok.Getter;

@Getter
public enum ReferralTypeEnum {
    DEFAULT(1),
    CUSTOM(2);

    private final int value;

    ReferralTypeEnum(int value) {
        this.value = value;
    }
}
