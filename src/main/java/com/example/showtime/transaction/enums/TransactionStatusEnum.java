package com.example.showtime.transaction.enums;

import lombok.Getter;

@Getter
public enum TransactionStatusEnum {
    INITIATED(1),
    PENDING(2),
    SUCCESS(3),
    FAILED(4),
    CANCELLED(5),
    REFUNDED(6);

    private final int value;

    TransactionStatusEnum(int value) {
        this.value = value;
    }
}
