package com.example.showtime.ticket_verify.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.ticket_verify.model.request.CreateVerifierRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifierValidationRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifyRequest;
import com.example.showtime.ticket_verify.model.response.TicketVerifierValidationResponse;
import com.example.showtime.ticket_verify.service.ITicketVerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://www.countersbd.com", "https://admin.countersbd.com", "https://checker.countersbd.com"}, maxAge = 3600)
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket-verify")
@Slf4j
public class TicketVerifyController {

    private final ITicketVerifyService ticketVerifyService;

    @PostMapping("/validate-verifier")
    public ResponseEntity<ApiResponse<TicketVerifierValidationResponse>> validateVerifier(@RequestBody TicketVerifierValidationRequest ticketVerifierValidationRequest) {
        log.info("Validating verifier");

        TicketVerifierValidationResponse verifierResponse = ticketVerifyService.validateVerifier(ticketVerifierValidationRequest);

        ApiResponse<TicketVerifierValidationResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Verifier validated successfully", verifierResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("create-verifier")
    public ResponseEntity<ApiResponse<?>> createVerifier(@RequestBody CreateVerifierRequest createVerifierRequest) {
        log.info("Creating verifier");

        ticketVerifyService.createVerifier(createVerifierRequest);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Verifier created successfully", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-ticket")
    public ResponseEntity<ApiResponse<?>> verifyTicket(@RequestBody TicketVerifyRequest ticketVerifyRequest) {
        log.info("Verifying ticket {}", ticketVerifyRequest.getTicketId());

        ticketVerifyService.verifyTicket(ticketVerifyRequest);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Ticket verified successfully", null);

        return ResponseEntity.ok(response);
    }
}
