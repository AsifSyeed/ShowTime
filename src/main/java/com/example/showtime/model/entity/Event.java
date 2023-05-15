package com.example.showtime.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity(name = "EVENT")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_sequence")
    @SequenceGenerator(name = "event_sequence", sequenceName = "event_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "EVENT_NAME")
    private String eventName;

    @Column(name = "EVENT_CAPACITY")
    private Long eventCapacity;

    @Column(name = "EVENT_START_DATE")
    private Date eventStartDate;

    @Column(name = "EVENT_END_DATE")
    private Date eventEndDate;

    @Column(name = "EVENT_QR_CODE", columnDefinition = "TEXT")
    private String eventQrCode;

    @Column(name = "EVENT_ACTIVE_STATUS")
    private  Boolean isActive;
}
