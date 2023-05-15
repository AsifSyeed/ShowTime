package com.example.showtime.service;

import com.example.showtime.model.request.EventRequest;
import com.example.showtime.model.response.EventResponse;

public interface IEventService {
    EventResponse createNewEvent(EventRequest eventRequest);
}
