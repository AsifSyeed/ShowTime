package com.example.showtime.ticket.service;

import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.request.CheckTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;

import java.util.List;

public interface ITicketService {
    BuyTicketResponse createTicket(BuyTicketRequest buyTicketRequest);
    Ticket getTicketById(Long ticketId);
    void markTicketAsUsed(Long ticketId);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByEventId(String eventId);

    Ticket getTicketByTicketId(String ticketId);

    List<Ticket> getTicketListByTransactionRefNo(String transactionRefNo);

    List<MyTicketResponse> getMyTickets();

    void updateTicketStatus(List<Ticket> selectedTickets, int transactionStatus);

    void verifyTicket(CheckTicketRequest checkTicketRequest);
}
