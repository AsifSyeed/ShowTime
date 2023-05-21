package com.example.showtime.event.model.request;
import javax.persistence.Column;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventRequest {
    private String eventName;

    private Long eventCapacity;

    private Date eventStartDate;

    private Date eventEndDate;

    private String eventQrCode;
}
