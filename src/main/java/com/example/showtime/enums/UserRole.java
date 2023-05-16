package com.example.showtime.enums;


public enum UserRole {
    NORMAL_USER(1),
    EVENT_CREATOR(2);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
