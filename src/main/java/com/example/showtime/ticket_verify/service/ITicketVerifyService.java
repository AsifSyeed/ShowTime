package com.example.showtime.ticket_verify.service;

import com.example.showtime.ticket_verify.model.request.CreateVerifierRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifierValidationRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifyRequest;
import com.example.showtime.ticket_verify.model.response.TicketVerifierValidationResponse;

public interface ITicketVerifyService {
    TicketVerifierValidationResponse validateVerifier(TicketVerifierValidationRequest ticketVerifierValidationRequest);

    void createVerifier(CreateVerifierRequest createVerifierRequest);

    void verifyTicket(TicketVerifyRequest ticketVerifyRequest);
}
