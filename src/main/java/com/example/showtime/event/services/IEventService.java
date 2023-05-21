package com.example.showtime.event.services;

import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.EventResponse;

import java.util.List;

public interface IEventService {
    EventResponse createNewEvent(EventRequest eventRequest);

    List<EventResponse> getAllEvents();
}
