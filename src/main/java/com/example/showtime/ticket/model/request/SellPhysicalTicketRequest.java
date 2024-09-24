package com.example.showtime.ticket.model.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellPhysicalTicketRequest {
    private String ticketOwnerName;
    private String ticketOwnerNumber;
    private String ticketOwnerEmail;
    private String physicalTicketId;
    private String eventId;
}
