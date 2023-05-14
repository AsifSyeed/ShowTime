package com.example.showtime.service;

import com.example.showtime.model.request.EventRequest;

public interface IEventService {
    void createNewEvent(EventRequest eventRequest);
}
