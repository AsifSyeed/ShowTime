package com.example.showtime.ticket.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "PHYSICAL_TICKET")
public class PhysicalTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PHYSCIAL_TICKET_ID", unique = true)
    private String physicalTicketId;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "IS_USED")
    private boolean isUsed;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    @Column(name = "ONLINE_TICKET_ID")
    private String onlineTicketId;

    @Column(name = "TICKET_OWNER_NAME")
    private String ticketOwnerName;

    @Column(name = "TICKET_OWNER_NUMBER")
    private String ticketOwnerNumber;

    @Column(name = "TICKET_OWNER_EMAIL")
    private String ticketOwnerEmail;

    @Column(name = "TICKET_CATEGORY")
    private Long ticketCategory;

    @Column(name ="VERIFIER_ID")
    private String verifierId;
}
