package com.example.showtime.ticket.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequest {
    private String categoryName;
    private Double categoryPrice;
    private Long categoryCapacity;
    private String categoryDescription;
}
