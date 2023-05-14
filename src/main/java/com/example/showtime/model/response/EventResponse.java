package com.example.showtime.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventResponse {
    private String eventName;
    private long eventCapacity;
    private String eventQrCode;
}
