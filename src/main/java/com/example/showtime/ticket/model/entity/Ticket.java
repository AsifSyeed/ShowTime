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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT")
    private Event event;

    @Column(name = "TICKET_QR_CODE", unique = true)
    private String ticketQrCode;

    @Column(name = "VALIDITY_DATE")
    private Date validityDate;

    @Column(name = "USED")
    private boolean used;

    @Column(name = "EVENT_QRCODE")
    private String eventQrCode;
 }
