package com.example.showtime.event.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "EVENT")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_NAME")
    private String eventName;

    @Column(name = "EVENT_CAPACITY")
    private Long eventCapacity;

    @Column(name = "EVENT_AVAILABLE_COUNT")
    private Long eventAvailableCount;

    @Column(name = "EVENT_START_DATE")
    private Date eventStartDate;

    @Column(name = "EVENT_END_DATE")
    private Date eventEndDate;

    @Column(name = "EVENT_ID", columnDefinition = "TEXT", unique = true)
    private String eventId;

    @Column(name = "EVENT_ACTIVE_STATUS")
    private  Boolean isActive;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "EVENT_IMAGE_URL")
    private String eventImageUrl;
}
