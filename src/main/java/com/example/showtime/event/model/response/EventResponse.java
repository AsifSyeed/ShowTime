package com.example.showtime.event.model.response;

import com.example.showtime.ticket.model.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventResponse {
    private String eventName;
    private long eventCapacity;
    private long eventAvailableTickets;
    private String eventId;
    private  String eventStartDate;
    private  String eventEndDate;
    private String eventBannerUrl;
    private String eventThumbnailUrl;
    private List<Category> categoryList;
    private String eventDescription;
}
