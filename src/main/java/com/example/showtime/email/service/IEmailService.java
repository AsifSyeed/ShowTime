package com.example.showtime.email.service;

import com.example.showtime.ticket.model.entity.Ticket;

import java.util.List;

public interface IEmailService {
    void sendTicketConfirmationMail(List<Ticket> transactionTicketList);

    void sendOtp(String email, String otp);
}
