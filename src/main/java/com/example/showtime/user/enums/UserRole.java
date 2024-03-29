package com.example.showtime.user.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    USER(1),
    ADMIN(2);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

}
