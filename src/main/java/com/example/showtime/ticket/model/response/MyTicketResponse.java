package com.example.showtime.ticket.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MyTicketResponse {
    private String ticketId;
    private String eventName;
    private String validityDate;
    private String ticketPrice;
    private String ticketOwnerName;
    private String ticketOwnerEmail;
    private String ticketOwnerNumber;
    private boolean ticketStatus;
    private String ticketCategoryName;
}
