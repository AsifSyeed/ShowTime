package com.example.showtime.ticket.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.*;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;
import com.example.showtime.ticket.service.IPhysicalTicketService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://www.countersbd.com", "https://admin.countersbd.com", "https://checker.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketController {
    private final ITicketService ticketService;
    private final IPhysicalTicketService physicalTicketService;

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

    @GetMapping("/send-email")
    public ResponseEntity<ApiResponse<?>> sendEmail(@RequestParam String ticketId) {
        ticketService.sendEmail(ticketId);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Email sent successfully", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-physical-ticket")
    public ResponseEntity<ApiResponse<?>> createPhysicalTicket(@RequestBody @Valid CreatePhysicalTicketRequest createPhysicalTicketRequest) {
        ticketService.createPhysicalTicket(createPhysicalTicketRequest);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Physical ticket created successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tag-physical-ticket")
    public ResponseEntity<ApiResponse<?>> tagPhysicalTicket(@RequestBody @Valid TagPhysicalTicketRequest tagPhysicalTicketRequest) {
        ticketService.tagPhysicalTicket(tagPhysicalTicketRequest);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Physical ticket tagged successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell-physical-ticket")
    public ResponseEntity<ApiResponse<?>> sellPhysicalTicket(@RequestBody @Valid SellPhysicalTicketRequest sellPhysicalTicketRequest) {
        physicalTicketService.sellPhysicalTicket(sellPhysicalTicketRequest);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Physical ticket sold successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/send-email-to-failed-transaction")
    public ResponseEntity<ApiResponse<?>> sendEmailToFailedTransaction() {
        ticketService.sendEmailToFailedTransaction();
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Email sent successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-all")
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        List<Ticket> myTicketResponses = ticketService.getTicketsByTransactionStatus(TransactionStatusEnum.SUCCESS.getValue());

        ApiResponse<List<Ticket>> response = new ApiResponse<>(HttpStatus.OK.value(), "Tickets retrieved successfully", myTicketResponses);

        return ResponseEntity.ok(response);
    }
}
