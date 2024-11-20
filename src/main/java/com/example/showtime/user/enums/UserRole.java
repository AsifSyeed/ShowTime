package com.example.showtime.user.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    SUPER_ADMIN(0),
    USER(1),
    ADMIN(2),
    TICKET_VERIFIER(3);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

}
