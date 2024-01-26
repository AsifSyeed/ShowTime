package com.example.showtime.ticket.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "CATEGORY")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(name = "CATEGORY_NAME")
    private String categoryName;

    @Column(name = "CATEGORY_DESCRIPTION")
    private String categoryDescription;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "CATEGORY_CAPACITY")
    private Long categoryCapacity;

    @Column(name = "CATEGORY_PRICE")
    private Double categoryPrice;

    @Column(name = "CATEGORY_AVAILABLE_COUNT")
    private Long categoryAvailableCount;
}
