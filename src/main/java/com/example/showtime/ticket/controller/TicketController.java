package com.example.showtime.ticket.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.service.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketController {
    private final ITicketService ticketService;

    @RequestMapping("/buy")
    public ResponseEntity<ApiResponse<List<BuyTicketResponse>>> buyTicket(@RequestBody @Valid BuyTicketRequest buyTicketRequest) {

        List<BuyTicketResponse> buyTicketResponse = ticketService.createTicket(buyTicketRequest);

        ApiResponse<List<BuyTicketResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Event created successfully", buyTicketResponse);

        return ResponseEntity.ok(response);
    }
}
