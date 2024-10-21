package com.example.showtime.admin.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySalesInfo {
    private String categoryName;
    private Long categoryId;
    private String eventId;
    private Long totalPurchasedTicket;
    private Double totalRevenue;
}
