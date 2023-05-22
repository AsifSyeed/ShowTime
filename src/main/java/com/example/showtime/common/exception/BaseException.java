package com.example.showtime.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private int responseCode;
    private String message;

    public BaseException(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }
}
