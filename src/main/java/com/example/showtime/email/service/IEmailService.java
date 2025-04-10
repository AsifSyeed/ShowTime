package com.example.showtime.email.service;

import com.example.showtime.ticket.model.entity.Ticket;

import java.util.List;

public interface IEmailService {
    void sendTicketConfirmationMail(List<Ticket> transactionTicketList);

    void sendGenericEmail(String email, String subject, String htmlContent);
}
