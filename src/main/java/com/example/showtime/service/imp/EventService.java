package com.example.showtime.service.imp;
import com.example.showtime.model.entity.Event;
import com.example.showtime.model.request.EventRequest;
import com.example.showtime.model.response.EventResponse;
import com.example.showtime.repository.EventRepository;
import com.example.showtime.service.IEventService;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventResponse createNewEvent(EventRequest eventRequest) {

        validateRequest(eventRequest);

        if (isEventNameExists(eventRequest.getEventName())) {
            throw new IllegalArgumentException("Event name already exists");
        }

        Event event = prepareEventModel(eventRequest);

        eventRepository.save(event);

        return EventResponse.builder()
                .eventName(event.getEventName())
                .eventCapacity(event.getEventCapacity())
                .eventQrCode(event.getEventQrCode())
                .build();
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
        event.setEventQrCode(eventRequest.getEventQrCode());

        return event;
    }

    private void validateRequest(EventRequest eventRequest) {
        if (Objects.isNull(eventRequest) ||
                StringUtils.isEmpty(eventRequest.getEventName()) ||
                Objects.isNull(eventRequest.getEventStartDate()) ||
                Objects.isNull(eventRequest.getEventEndDate()) ||
                Objects.isNull(eventRequest.getEventCapacity())) {

            throw new InvalidRequestStateException("Request body is not valid");
        }
    }
}
