package com.example.showtime.refund.model.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    private String transactionRefNo;
    private String remarks;
}
