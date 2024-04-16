package com.example.showtime.email.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.email.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailController {
    private final IEmailService emailService;

    @GetMapping("/ticket-confirmation/{transactionRefNo}")
    public ResponseEntity<ApiResponse<?>> sendTicketConfirmationMail(@PathVariable @Valid String transactionRefNo) {
        emailService.sendTicketConfirmationMail(transactionRefNo);

        ApiResponse<?> response = new ApiResponse<>(200, "Email sent successfully", null);

        return ResponseEntity.ok(response);
    }
}
