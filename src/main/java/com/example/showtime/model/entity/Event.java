package com.example.showtime.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity(name = "EVENT")
public class Event {

    @Id
    @GeneratedValue
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
}
