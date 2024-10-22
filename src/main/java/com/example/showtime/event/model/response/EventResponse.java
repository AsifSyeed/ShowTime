package com.example.showtime.event.model.response;

import com.example.showtime.referral.model.response.GetReferralResponse;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
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
    private List<EventCategoryResponse> categoryList;
    private String eventDescription;
    private List<GetReferralResponse> referralList;
    private String eventLocation;
}
