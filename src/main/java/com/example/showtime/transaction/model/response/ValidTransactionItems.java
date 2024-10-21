package com.example.showtime.transaction.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidTransactionItems {
    private String amount;
    private String eventName;
    private String transactionRefNo;
    private String userEmail;
    private String userName;
    private String userPhone;
    private Integer numberOfTickets;
    private String createdDate;
}
