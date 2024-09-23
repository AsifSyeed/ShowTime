package com.example.showtime.ticket.model.request;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BuyTicketRequest {
    private String eventId;
    private Long ticketCategory;
    private Double totalPrice;
    private String couponCode;
    private List<TicketOwnerInformationRequest> ticketOwnerInformation;
}
