package com.example.showtime.ticket.enums;

import lombok.Getter;

@Getter
public enum TicketTypeEnum {
    ONLINE(1),
    PHYSICAL(2),
    BOTH(3);

    private final int value;

    TicketTypeEnum(int value) {
        this.value = value;
    }
}
