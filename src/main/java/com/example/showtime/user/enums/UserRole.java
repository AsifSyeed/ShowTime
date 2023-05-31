package com.example.showtime.user.enums;


public enum UserRole {
    CUSTOMER(1),
    ORGANIZER(2);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
