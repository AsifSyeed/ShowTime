package com.example.showtime.ticket_verify.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVerifierRequest {
    private String eventId;
    private Long categoryId;
}
