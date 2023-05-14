package com.example.showtime.service.imp;
import com.example.showtime.model.entity.Event;
import com.example.showtime.model.request.EventRequest;
import com.example.showtime.repository.EventRepository;
import com.example.showtime.service.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewEvent(EventRequest eventRequest) {
        //check validation first
        Event event = prepareEventModel(eventRequest);

        eventRepository.save(event);
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
}
