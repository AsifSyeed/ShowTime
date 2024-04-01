package com.example.showtime.ticket.repository;

import com.example.showtime.ticket.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventId(String eventId);

    List<Ticket> findByTicketTransactionId(String transactionRefNo);
}