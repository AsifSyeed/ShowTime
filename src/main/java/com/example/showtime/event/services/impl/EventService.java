package com.example.showtime.event.services.impl;

import com.example.showtime.admin.model.entity.Admin;
import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.request.EventRequest;
import com.example.showtime.event.model.response.AdminEventResponse;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.event.repository.EventRepository;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.referral.repository.ReferralRepository;
import com.example.showtime.ticket.model.request.CategoryRequest;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final AdminRepository adminRepository;
    private final ICategoryService categoryService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewEvent(EventRequest eventRequest) {

        try {
            validateRequest(eventRequest);

            Event event = prepareEventModel(eventRequest);
            categoryService.createCategory(eventRequest.getCategoryList(), event.getEventId());

            eventRepository.save(event);
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> EventResponse.builder()
                        .eventName(event.getEventName())
                        .eventCapacity(event.getEventCapacity())
                        .eventAvailableTickets(event.getEventAvailableCount())
                        .eventId(event.getEventId())
                        .eventStartDate(String.valueOf(event.getEventStartDate()))
                        .eventEndDate(String.valueOf(event.getEventEndDate()))
                        .eventBannerUrl(event.getEventBannerUrl())
                        .eventThumbnailUrl(event.getEventThumbnailUrl())
                        .eventDescription(event.getEventDescription())
                        .eventLocation(event.getEventLocation())
                        .categoryList(categoryService.getAllCategoriesByEventId(event.getEventId()))
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isEventNameExists(String eventName) {
        return eventRepository.existsByEventName(eventName);
    }

    public boolean isEventIdExists(String eventId) {
        return eventRepository.existsByEventId(eventId);
    }

    @Override
    public Event getEventById(String eventId) {
        return eventRepository.findByEventId(eventId);
    }

    @Override
    public List<Event> getEventByCreatedBy(String createdBy) {
        return eventRepository.findByCreatedBy(createdBy);
    }

    @Override
    public void updateAvailableTickets(String eventId, long categoryId, long size) {
        Event event = getEventById(eventId);
        event.setEventAvailableCount(event.getEventAvailableCount() - size);
        categoryService.updateAvailableTickets(categoryId, eventId, size);

        eventRepository.save(event);
    }

    @Override
    public EventResponse checkEventToVerify(String eventId) {
        Event event = getEventById(eventId);

        return EventResponse.builder()
                .eventName(event.getEventName())
                .eventCapacity(event.getEventCapacity())
                .eventAvailableTickets(event.getEventAvailableCount())
                .eventId(event.getEventId())
                .eventStartDate(String.valueOf(event.getEventStartDate()))
                .eventEndDate(String.valueOf(event.getEventEndDate()))
                .eventBannerUrl(event.getEventBannerUrl())
                .eventThumbnailUrl(event.getEventThumbnailUrl())
                .eventDescription(event.getEventDescription())
                .categoryList(categoryService.getAllCategoriesByEventId(event.getEventId()))
                .build();
    }

    @Override
    public List<AdminEventResponse> getAllEventsForAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        // check if userRole is null or not either SUPER_ADMIN or ADMIN
        if (userRole == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User Role not found");
        }

        if (Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue() && Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized");
        }

        if (Integer.parseInt(userRole) == UserRole.SUPER_ADMIN.getValue()) {
            return eventRepository.findAll().stream()
                    .map(event -> AdminEventResponse.builder()
                            .id(event.getId())
                            .eventName(event.getEventName())
                            .eventCapacity(event.getEventCapacity())
                            .eventAvailableCount(event.getEventAvailableCount())
                            .eventStartDate(String.valueOf(event.getEventStartDate()))
                            .eventEndDate(String.valueOf(event.getEventEndDate()))
                            .eventId(event.getEventId())
                            .isActive(event.getIsActive())
                            .createdBy(event.getCreatedBy())
                            .eventBannerUrl(event.getEventBannerUrl())
                            .eventThumbnailUrl(event.getEventThumbnailUrl())
                            .eventDescription(event.getEventDescription())
                            .eventLocation(event.getEventLocation())
                            .categoryList(categoryService.getAllCategoriesByEventId(event.getEventId()))
                            .build())
                    .collect(Collectors.toList());
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            return eventRepository.findByCreatedBy(userEmail).stream()
                    .map(event -> AdminEventResponse.builder()
                            .id(event.getId())
                            .eventName(event.getEventName())
                            .eventCapacity(event.getEventCapacity())
                            .eventAvailableCount(event.getEventAvailableCount())
                            .eventStartDate(String.valueOf(event.getEventStartDate()))
                            .eventEndDate(String.valueOf(event.getEventEndDate()))
                            .eventId(event.getEventId())
                            .isActive(event.getIsActive())
                            .createdBy(event.getCreatedBy())
                            .eventBannerUrl(event.getEventBannerUrl())
                            .eventThumbnailUrl(event.getEventThumbnailUrl())
                            .eventDescription(event.getEventDescription())
                            .eventLocation(event.getEventLocation())
                            .categoryList(categoryService.getAllCategoriesByEventId(event.getEventId()))
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<EventCategoryResponse> getCategoryList(String eventId) {
        return categoryService.getAllCategoriesByEventId(eventId);
    }

    private Event prepareEventModel(EventRequest eventRequest) {
        Event event = new Event();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        Admin createdBy = adminRepository.findByEmail(createdByUserEmail)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Admin not found"));

        event.setEventName(eventRequest.getEventName());
        event.setEventStartDate(eventRequest.getEventStartDate());
        event.setEventEndDate(eventRequest.getEventEndDate());
        event.setEventCapacity(eventRequest.getEventCapacity());
        event.setEventAvailableCount(eventRequest.getEventCapacity());
        event.setEventId(generateEventId(eventRequest.getEventName()));
        event.setIsActive(getEventStatus(eventRequest.getEventEndDate()));
        event.setCreatedBy(createdBy.getEmail());

        return event;
    }

    private Boolean getEventStatus(Date eventEndDate) {
        Date currentDate = new Date();
        return eventEndDate.after(currentDate);
    }

    private void validateRequest(EventRequest eventRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create an event");
        }

        if (Objects.isNull(eventRequest) ||
                StringUtils.isEmpty(eventRequest.getEventName()) ||
                Objects.isNull(eventRequest.getEventStartDate()) ||
                Objects.isNull(eventRequest.getEventEndDate()) ||
                Objects.isNull(eventRequest.getEventCapacity())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        if (isEventNameExists(eventRequest.getEventName())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event name already exists");
        }

        if (eventRequest.getEventEndDate().before(eventRequest.getEventStartDate())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event end date cannot be greater than event start date");
        }

        if (checkTotalCapacity(eventRequest.getCategoryList()) > eventRequest.getEventCapacity()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Total capacity is not valid");
        }
    }

    private Long checkTotalCapacity(List<CategoryRequest> categoryList) {
        Long totalCapacity = 0L;

        for (CategoryRequest category : categoryList) {
            totalCapacity += category.getCategoryCapacity();
        }

        return totalCapacity;
    }

    private String generateEventId(String eventName) {
        // concat all the characters of the event name
        String eventId = eventName.replaceAll("\\s", "");
        // return all the characters of the event name in lowercase
        return eventId.toLowerCase();
    }
}
