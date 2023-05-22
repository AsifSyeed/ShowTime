package com.example.showtime.ticket.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.repository.TicketRepository;
import com.example.showtime.ticket.service.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final TicketRepository ticketRepository;

    @Override
    public void createTicket(Event event, int ticketNo) {
        Ticket ticket = new Ticket();
        ticket.setTicketQrCode(generateQRCode(event, ticketNo));
        ticket.setValidityDate(event.getEventEndDate());
        ticket.setUsed(false);
        ticket.setEvent(event);
        ticket.setEventQrCode(event.getEventQrCode());
        ticketRepository.save(ticket);
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found"));
    }

    @Override
    public void markTicketAsUsed(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> getTicketsByEvent(Event event) {
        return ticketRepository.findByEventQrCode(event.getEventQrCode());
    }

    private String generateQRCode(Event event, int ticketNo) {

        return event.getEventQrCode() + event.getId() + ticketNo; // Placeholder for the actual generation code
    }
}