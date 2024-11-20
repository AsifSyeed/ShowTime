package com.example.showtime.event.model.response;

import com.example.showtime.referral.model.response.GetReferralResponse;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEventResponse {
    private Long id;
    private String eventName;
    private Long eventCapacity;
    private Long eventAvailableCount;
    private String eventStartDate;
    private String eventEndDate;
    private String eventId;
    private Boolean isActive;
    private String createdBy;
    private String eventBannerUrl;
    private String eventThumbnailUrl;
    private String eventDescription;
    private String eventLocation;
    private List<EventCategoryResponse> categoryList;
    private List<GetReferralResponse> referralList;
}
