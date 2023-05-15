package com.example.showtime.service;

import com.example.showtime.model.request.EventRequest;
import com.example.showtime.model.response.EventResponse;

import java.util.List;

public interface IEventService {
    EventResponse createNewEvent(EventRequest eventRequest);

    List<EventResponse> getAllEvents();
}
