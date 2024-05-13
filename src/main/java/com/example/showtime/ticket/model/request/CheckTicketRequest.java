package com.example.showtime.ticket.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CheckTicketRequest {
    private String ticketId;
}
