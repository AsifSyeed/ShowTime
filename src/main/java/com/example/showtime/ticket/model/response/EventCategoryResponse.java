package com.example.showtime.ticket.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Long categoryCapacity;
    private Double categoryPrice;
    private Long categoryAvailableCount;
    private Long maximumQuantity;
    private Double discountedPrice;
}
