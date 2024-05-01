package com.example.showtime.transaction.model.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CheckTransactionStatusResponse {
    private String transactionRefNo;
    private int transactionStatus;
    private double totalAmount;
    private int numberOfTickets;
    private String eventName;
}
