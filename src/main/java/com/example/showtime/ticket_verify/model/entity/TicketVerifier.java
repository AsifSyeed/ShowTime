package com.example.showtime.ticket_verify.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "TICKET_VERIFIER")
public class TicketVerifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "VERIFIER_ID", unique = true)
    private String verifierId;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "CATEGORY_ID")
    private Long categoryId;
}
