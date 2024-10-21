package com.example.showtime.ticket.repository;

import com.example.showtime.ticket.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventId(String eventId);

    List<Ticket> findByTicketTransactionId(String transactionRefNo);

    List<Ticket> findByTicketCreatedBy(String email);

    Ticket findByTicketId(String ticketId);

    List<Ticket> findByTicketCreatedByOrderByTicketCreatedDateDesc(String email);

    Ticket findByTicketIdAndTicketCreatedBy(String ticketId, String email);

    List<Ticket> findByTicketTransactionStatus(int value);

    List<Ticket> findByTicketTransactionStatusNot(int value);

    List<Ticket> findByEventIdInAndTicketTransactionStatus(List<String> eventIds, int value);

    List<Ticket> findByEventIdAndTicketTransactionStatus(String eventId, int value);

    List<Ticket> findByTicketCreatedByAndTicketTransactionStatus(String userEmail, int value);
}