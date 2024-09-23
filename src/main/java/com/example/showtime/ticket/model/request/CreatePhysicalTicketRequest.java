package com.example.showtime.ticket.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePhysicalTicketRequest {
    private String eventId;
    private Long quantity;
}
