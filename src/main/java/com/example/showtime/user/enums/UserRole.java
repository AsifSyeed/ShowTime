package com.example.showtime.user.enums;


public enum UserRole {
    USER(1),
    ADMIN(2);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
