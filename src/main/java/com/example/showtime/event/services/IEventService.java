package com.example.showtime.event.services;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.AdminEventResponse;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.ticket.model.response.EventCategoryResponse;

import java.util.List;

public interface IEventService {
    void createNewEvent(EventRequest eventRequest);

    List<EventResponse> getAllEvents();

    boolean isEventIdExists(String eventId);

    Event getEventById(String eventId);

    List<Event> getEventByCreatedBy(String createdBy);

    void updateAvailableTickets(String eventId, long categoryId, long size);

    EventResponse checkEventToVerify(String eventId);

    List<AdminEventResponse> getAllEventsForAdmin();

    List<EventCategoryResponse> getCategoryList(String eventId);
}
