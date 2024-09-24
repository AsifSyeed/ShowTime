package com.example.showtime.ticket.repository;

import com.example.showtime.ticket.model.entity.PhysicalTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalTicketRepository extends JpaRepository<PhysicalTicket, Long> {
    List<PhysicalTicket> findByEventId(String eventId);

    PhysicalTicket findByPhysicalTicketId(String physicalTicketId);

    PhysicalTicket findByPhysicalTicketIdAndEventId(String physicalTicketId, String eventId);
}
