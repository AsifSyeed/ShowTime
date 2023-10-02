package com.example.showtime.event.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventResponse {
    private String eventName;
    private long eventCapacity;
    private String eventId;
    private  String eventStartDate;
    private  String eventEndDate;
}
