package com.example.showtime.controller;
import com.example.showtime.model.entity.Event;
import com.example.showtime.model.request.EventRequest;
import com.example.showtime.model.response.ApiResponse;
import com.example.showtime.model.response.EventResponse;
import com.example.showtime.service.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {

    private final IEventService iEventService;
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@RequestBody @Valid EventRequest eventRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            // If there are validation errors, return a Bad Request response with the error details
            return ResponseEntity.badRequest().build();
        }

        Event event = iEventService.createNewEvent(eventRequest);

        EventResponse eventResponse = new EventResponse(
                event.getEventName(),
                event.getEventCapacity(),
                event.getEventQrCode()
        );

        // Create a success response with a response code, message, and event response data
        ApiResponse<EventResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Event created successfully", Collections.singletonList(eventResponse));

        return ResponseEntity.ok(response);
    }
}
