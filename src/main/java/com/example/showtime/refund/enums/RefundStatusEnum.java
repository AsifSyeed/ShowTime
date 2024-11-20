package com.example.showtime.refund.enums;

import lombok.Getter;

@Getter
public enum RefundStatusEnum {
    SUCCESS(1, "success"),
    FAILED(2, "failed"),
    PROCESSING(3, "processing");

    private final int value;
    private final String stringValue;

    RefundStatusEnum(int value, String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public static String getStringValueFromInt(int value) {
        for (RefundStatusEnum status : RefundStatusEnum.values()) {
            if (status.getValue() == value) {
                return status.getStringValue();
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    // Method to map from string to RefundStatusEnum
    public static RefundStatusEnum fromString(String status) {
        switch (status.toLowerCase()) {
            case "success":
                return SUCCESS;
            case "failed":
                return FAILED;
            case "processing":
                return PROCESSING;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}
