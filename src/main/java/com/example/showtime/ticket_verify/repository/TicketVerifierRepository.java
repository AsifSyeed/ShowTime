package com.example.showtime.ticket_verify.repository;

import com.example.showtime.ticket_verify.model.entity.TicketVerifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketVerifierRepository extends JpaRepository<TicketVerifier, Long> {
    TicketVerifier findByVerifierId(String verifierId);
}
