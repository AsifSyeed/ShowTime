package com.example.showtime.ticket.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagPhysicalTicketRequest {
    private String onlineTicketId;
    private String physicalTicketId;
}
