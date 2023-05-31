package com.example.showtime.ticket.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BuyTicketRequest {
    private String eventId;
    private Long numberOfTicket;
}
