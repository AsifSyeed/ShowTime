package com.example.showtime.refund.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private String refundStatus;
    private String refundId;
    private String transactionId;
    private String errorMessage;
}
