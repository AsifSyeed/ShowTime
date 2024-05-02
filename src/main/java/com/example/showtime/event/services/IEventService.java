package com.example.showtime.event.services;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.EventResponse;

import java.util.List;

public interface IEventService {
    void createNewEvent(EventRequest eventRequest);

    List<EventResponse> getAllEvents();

    boolean isEventIdExists(String eventId);

    Event getEventById(String eventId);

    void updateAvailableTickets(String eventId, long categoryId, long size);
}
