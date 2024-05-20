package com.example.showtime.tfa.enums;

import lombok.Getter;

@Getter
public enum FeatureEnum {
    SIGN_UP(1),
    TRANSACTION(2),
    FORGOT_PASSWORD(3),
    CHANGE_PASSWORD(4);

    private final int value;

    FeatureEnum(int value) {
        this.value = value;
    }
}
