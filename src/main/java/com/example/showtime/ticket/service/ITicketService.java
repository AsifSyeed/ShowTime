package com.example.showtime.ticket.service;

import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.request.CheckTicketRequest;
import com.example.showtime.ticket.model.request.CreatePhysicalTicketRequest;
import com.example.showtime.ticket.model.request.TagPhysicalTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;

import javax.validation.Valid;
import java.util.List;

public interface ITicketService {
    BuyTicketResponse createTicket(BuyTicketRequest buyTicketRequest);
    Ticket getTicketById(Long ticketId);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByEventId(String eventId);

    Ticket getTicketByTicketId(String ticketId);

    List<Ticket> getTicketListByTransactionRefNo(String transactionRefNo);

    List<MyTicketResponse> getMyTickets();

    void updateTicketStatus(List<Ticket> selectedTickets, int transactionStatus);

    void verifyTicket(CheckTicketRequest checkTicketRequest);

    void sendEmail(String ticketId);

    void createPhysicalTicket(CreatePhysicalTicketRequest physicalTicketRequest);

    void tagPhysicalTicket(TagPhysicalTicketRequest tagPhysicalTicketRequest);

    void sendEmailToFailedTransaction();

    List<Ticket> getTicketsByTransactionStatus(int value);

    List<Ticket> getTicketsByEventIdsAndTransactionStatus(List<String> eventIds, int value);

    List<Ticket> getTicketsByEventIdAndTransactionStatus(String eventId, int value);
}
