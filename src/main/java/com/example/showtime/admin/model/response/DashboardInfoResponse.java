package com.example.showtime.admin.model.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardInfoResponse {
    private Long totalPurchasedTicket;
    private Double totalRevenue;
    private Long totalUser;
    private List<CategorySalesInfo> categorySalesInfoList;
}
