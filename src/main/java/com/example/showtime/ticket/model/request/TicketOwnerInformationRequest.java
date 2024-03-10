package com.example.showtime.ticket.model.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TicketOwnerInformationRequest {
    private String ticketOwnerName;
    private String ticketOwnerNumber;
    private String ticketOwnerEmail;
}
