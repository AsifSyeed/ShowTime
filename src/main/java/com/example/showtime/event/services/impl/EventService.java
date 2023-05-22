package com.example.showtime.event.services.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.event.repository.EventRepository;
import com.example.showtime.event.services.IEventService;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventResponse createNewEvent(EventRequest eventRequest) {

        try {
            validateRequest(eventRequest);

            Event event = prepareEventModel(eventRequest);

            eventRepository.save(event);

            return EventResponse.builder()
                    .eventName(event.getEventName())
                    .eventCapacity(event.getEventCapacity())
                    .eventQrCode(event.getEventQrCode())
                    .build();
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> EventResponse.builder()
                        .eventName(event.getEventName())
                        .eventCapacity(event.getEventCapacity())
                        .eventQrCode(event.getEventQrCode())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isEventNameExists(String eventName) {
        return eventRepository.existsByEventName(eventName);
    }

    private Event prepareEventModel(EventRequest eventRequest) {
        Event event = new Event();

        event.setEventName(eventRequest.getEventName());
        event.setEventStartDate(eventRequest.getEventStartDate());
        event.setEventEndDate(eventRequest.getEventEndDate());
        event.setEventCapacity(eventRequest.getEventCapacity());
        event.setEventQrCode(generateRandomString(eventRequest.getEventName()));
        event.setIsActive(getEventStatus(eventRequest.getEventEndDate()));

        return event;
    }

    private Boolean getEventStatus(Date eventEndDate) {
        Date currentDate = new Date();
        return eventEndDate.after(currentDate);
    }

    private void validateRequest(EventRequest eventRequest) {
        if (Objects.isNull(eventRequest) ||
                StringUtils.isEmpty(eventRequest.getEventName()) ||
                Objects.isNull(eventRequest.getEventStartDate()) ||
                Objects.isNull(eventRequest.getEventEndDate()) ||
                Objects.isNull(eventRequest.getEventCapacity())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        if (isEventNameExists(eventRequest.getEventName())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event name already exists");
        }

        if (eventRequest.getEventEndDate().before(eventRequest.getEventStartDate())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event end date cannot be greater than event start date");
        }
    }

    private String generateRandomString(String eventName) {
        StringBuilder randomString = new StringBuilder();

        // Split the event name by whitespace to get individual words
        String[] words = eventName.split("\\s+");

        // Iterate over each word and append its capitalized first letter to the random string
        for (String word : words) {
            if (!word.isEmpty()) {
                randomString.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        // Calculate the number of remaining characters needed
        int remainingCharacters = 10 - randomString.length();

        // Generate random numbers to fill up the remaining characters
        Random random = new Random();
        for (int i = 0; i < remainingCharacters; i++) {
            randomString.append(random.nextInt(10));
        }

        return randomString.toString();
    }
}
