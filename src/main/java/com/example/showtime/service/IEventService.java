package com.example.showtime.service;

import com.example.showtime.model.entity.Event;
import com.example.showtime.model.request.EventRequest;

public interface IEventService {
    Event createNewEvent(EventRequest eventRequest);
    boolean isEventNameExists(String eventName);
}
