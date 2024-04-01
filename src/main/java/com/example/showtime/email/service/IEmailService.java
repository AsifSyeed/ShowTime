package com.example.showtime.email.service;

import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;

public interface IEmailService {
    void sendTicketConfirmationMail(CheckTransactionStatusRequest checkTransactionStatusRequest);
}
