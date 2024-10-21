package com.example.showtime.event.controller;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.event.model.response.AdminEventResponse;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.event.services.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com", "https://admin.countersbd.com", "https://checker.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {

    private final IEventService iEventService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createEvent(@RequestBody @Valid EventRequest eventRequest) {

        iEventService.createNewEvent(eventRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Event created successfully", null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> eventResponses = iEventService.getAllEvents();

//        if (eventResponses.isEmpty()) {
//            ApiResponse<List<EventResponse>> notFoundResponse = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "No events found", null);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
//        }

        ApiResponse<List<EventResponse>> successResponse = new ApiResponse<>(HttpStatus.OK.value(), "Events retrieved successfully", eventResponses);

        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/admin/all")
    // add path variable for event id
    public ResponseEntity<ApiResponse<List<AdminEventResponse>>> getAllEventsForAdmin() {
        List<AdminEventResponse> eventResponses = iEventService.getAllEventsForAdmin();

        ApiResponse<List<AdminEventResponse>> successResponse = new ApiResponse<>(HttpStatus.OK.value(), "Events retrieved successfully", eventResponses);

        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/verify-for-scanner")
    public ResponseEntity<ApiResponse<EventResponse>> checkEventToVerify(@RequestParam String eventId) {
        EventResponse eventResponse = iEventService.checkEventToVerify(eventId);
        ApiResponse<EventResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Event retrieved successfully!", eventResponse);
        return ResponseEntity.ok(response);
    }
}
