package com.example.showtime.ticket.service;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;

import java.util.List;

public interface ITicketService {
    List<BuyTicketResponse> createTicket(BuyTicketRequest buyTicketRequest);
    Ticket getTicketById(Long ticketId);
    void markTicketAsUsed(Long ticketId);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByEventId(String eventId);
}
