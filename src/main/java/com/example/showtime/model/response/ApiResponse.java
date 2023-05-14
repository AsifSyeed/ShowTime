package com.example.showtime.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse<T> {
    private int responseCode;
    private String message;
    private List<T> data;

    public ApiResponse(int responseCode, String message, List<T> data) {
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
    }
}

