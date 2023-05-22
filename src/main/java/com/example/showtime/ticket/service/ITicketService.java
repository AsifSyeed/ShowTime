package com.example.showtime.ticket.service;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.ticket.model.entity.Ticket;

import java.util.List;

public interface ITicketService {
    void createTicket(Event event, int ticketNo);
    Ticket getTicketById(Long ticketId);
    void markTicketAsUsed(Long ticketId);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByEvent(Event event);
}
