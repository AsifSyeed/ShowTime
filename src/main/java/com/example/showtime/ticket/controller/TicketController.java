package com.example.showtime.ticket.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.request.CheckTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;
import com.example.showtime.ticket.service.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketController {
    private final ITicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<BuyTicketResponse>> buyTicket(@RequestBody @Valid BuyTicketRequest buyTicketRequest) {

        BuyTicketResponse buyTicketResponse = ticketService.createTicket(buyTicketRequest);

        ApiResponse<BuyTicketResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Ticket created successfully", buyTicketResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<MyTicketResponse>>> getMyTickets() {
        List<MyTicketResponse> myTicketResponses = ticketService.getMyTickets();

        ApiResponse<List<MyTicketResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Tickets retrieved successfully", myTicketResponses);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyTicket(@RequestBody @Valid CheckTicketRequest checkTicketRequest) {
        ticketService.verifyTicket(checkTicketRequest);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Ticket verified successfully", null);

        return ResponseEntity.ok(response);
    }
}
