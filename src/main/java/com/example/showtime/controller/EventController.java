package com.example.showtime.controller;
import com.example.showtime.model.request.EventRequest;
import com.example.showtime.service.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {

    private final IEventService iEventService;
    @PostMapping("/create")
    public ResponseEntity<Void> createEvent(@RequestBody EventRequest eventRequest) {

        iEventService.createNewEvent(eventRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
