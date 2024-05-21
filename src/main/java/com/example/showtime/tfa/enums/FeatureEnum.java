package com.example.showtime.tfa.enums;

import lombok.Getter;

@Getter
public enum FeatureEnum {
    SIGN_UP(1, "Sign up"),
    TRANSACTION(2, "Transaction"),
    FORGET_PASSWORD(3, "Forget Password"),
    CHANGE_PASSWORD(4, "Change Password");

    private final int value;
    private final String description;

    FeatureEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FeatureEnum fromValue(int value) {
        for (FeatureEnum feature : FeatureEnum.values()) {
            if (feature.getValue() == value) {
                return feature;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
    public static FeatureEnum fromDescription(String description) {
        for (FeatureEnum feature : FeatureEnum.values()) {
            if (feature.getDescription().equalsIgnoreCase(description)) {
                return feature;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }
}
