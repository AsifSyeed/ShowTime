package com.example.showtime.ticket.model.entity;

import com.example.showtime.event.model.entity.Event;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "TICKET")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TICKET_QR_CODE", unique = true)
    private String ticketQrCode;

    @Column(name = "VALIDITY_DATE")
    private Date validityDate;

    @Column(name = "USED")
    private boolean used;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "TICKET_OWNER_NAME")
    private String ticketOwnerName;

    @Column(name = "TICKET_OWNER_NUMBER")
    private String ticketOwnerNumber;

    @Column(name = "TICKET_OWNER_EMAIL")
    private String ticketOwnerEmail;

    @Column(name = "TICKET_CATEGORY")
    private Long ticketCategory;

    @Column(name = "TICKET_CREATED_BY")
    private String ticketCreatedBy;

    @Column(name = "TICKET_CREATED_DATE")
    private Date ticketCreatedDate;

    @Column(name = "TICKET_TRANSACTION_ID")
    private String ticketTransactionId;

    @Column(name = "TICKET_PRICE")
    private Double ticketPrice;
 }
