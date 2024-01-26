package com.example.showtime.event.model.request;

import com.example.showtime.ticket.model.request.CategoryRequest;
import lombok.*;

import java.util.Date;
import java.util.List;

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

    private List<CategoryRequest> categoryList;
}
