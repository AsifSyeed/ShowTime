package com.example.showtime.transaction.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CheckTransactionStatusRequest {
    private String transactionRefNo;
}
