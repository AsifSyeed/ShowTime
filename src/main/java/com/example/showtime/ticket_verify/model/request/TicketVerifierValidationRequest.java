package com.example.showtime.ticket_verify.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketVerifierValidationRequest {
    private String verifierId;
    private String eventId;
}
