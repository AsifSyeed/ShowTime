package com.example.showtime.ticket.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BuyTicketResponse {
    private String ticketId;
    private String eventId;
    private String userName;
}
