package com.example.showtime.referral.service.impl;

import com.example.showtime.admin.model.entity.Admin;
import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.repository.EventRepository;
import com.example.showtime.event.services.impl.EventService;
import com.example.showtime.referral.enums.ReferralTypeEnum;
import com.example.showtime.referral.model.request.CreateReferralRequest;
import com.example.showtime.referral.model.entity.Referral;
import com.example.showtime.referral.model.request.UpdateReferralRequest;
import com.example.showtime.referral.model.response.GetReferralResponse;
import com.example.showtime.referral.repository.ReferralRepository;
import com.example.showtime.referral.service.IReferralService;
import com.example.showtime.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService implements IReferralService {
    private final ReferralRepository referralRepository;
    private final AdminRepository adminRepository;
    private final EventRepository eventRepository;

    @Override
    public List<GetReferralResponse> getReferralByEventId(String eventId) {
        try {
            if (eventId == null) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event id is required");
            }

            return referralRepository.findByEventId(eventId).stream()
                    .filter(Referral::getIsActive) // Only include active referrals
                    .map(referral -> GetReferralResponse.builder()
                            .referralCode(referral.getReferralCode())
                            .eventId(referral.getEventId())
                            .referralDiscount(referral.getReferralDiscount())
                            .referralType(referral.getReferralType())
                            .build())
                    .collect(Collectors.toList());
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Referral> getAllReferral() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User Role not found.");
        }

        if (Integer.parseInt(userRole) != UserRole.ADMIN.getValue() && Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create a referral");
        }

        if (Integer.parseInt(userRole) == UserRole.SUPER_ADMIN.getValue()) {
            return referralRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            List<Event> events = eventRepository.findByCreatedBy(userEmail);

            List<String> eventIds = events.stream()
                    .map(Event::getEventId)
                    .collect(Collectors.toList());

            return referralRepository.findByEventIdIn(eventIds);
        }
    }

    @Override
    public void createReferral(CreateReferralRequest referral) {
        try {
            validateRequest(referral);

            Referral newReferral = prepareReferralModel(referral);

            referralRepository.save(newReferral);
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public void updateReferral(UpdateReferralRequest referral) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.ADMIN.getValue() || Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create a referral");
        }

        if (Objects.isNull(referral) ||
                Objects.isNull(referral.getId()) ||
                StringUtils.isEmpty(referral.getReferralCode()) ||
                Objects.isNull(referral.getReferralDiscount()) ||
                StringUtils.isEmpty(referral.getEventId()) ||
                Objects.isNull(referral.getReferralType()) ||
                Objects.isNull(referral.getIsActive())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Referral existingReferral = referralRepository.findByIdAndReferralCodeAndEventId(referral.getId(), referral.getReferralCode(), referral.getEventId());

        if (existingReferral == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Referral not found");
        }

        existingReferral.setReferralDiscount(referral.getReferralDiscount());
        existingReferral.setReferralType(referral.getReferralType());
        existingReferral.setIsActive(referral.getIsActive());

        if (existingReferral.getReferralType() == ReferralTypeEnum.DEFAULT.getValue()) {
            // set all other default referrals to custom
            Referral defaultReferrals = referralRepository.findByEventIdAndReferralType(existingReferral.getEventId(), ReferralTypeEnum.DEFAULT.getValue());
            defaultReferrals.setReferralType(ReferralTypeEnum.CUSTOM.getValue());
            referralRepository.save(defaultReferrals);
        }

        referralRepository.save(existingReferral);
    }

    @Override
    public Referral getDefaultReferral(String eventId) {
        return referralRepository.findByEventIdAndReferralType(eventId, ReferralTypeEnum.DEFAULT.getValue());
    }

    private Referral prepareReferralModel(CreateReferralRequest referral) {
        Referral newReferral = new Referral();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        Admin createdBy = adminRepository.findByEmail(createdByUserEmail)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Admin not found"));

        newReferral.setReferralCreatedBy(createdBy.getEmail());
        newReferral.setEventId(referral.getEventId());
        newReferral.setReferralCode(referral.getReferralCode());
        newReferral.setReferralDiscount(referral.getReferralDiscount());
        newReferral.setReferralCreatedDate(Calendar.getInstance().getTime());
        newReferral.setReferralType(referral.getReferralType());
        newReferral.setIsActive(true);

        return newReferral;
    }

    private void validateRequest(CreateReferralRequest referral) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User Role not found");
        }

        if (Integer.parseInt(userRole) != UserRole.ADMIN.getValue() && Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized to create a referral");
        }

        if (Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            Event event = eventRepository.findByEventId(referral.getEventId());

            if (!event.getCreatedBy().equals(userEmail)) {
                throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized to create a referral for this event");
            }
        }

        if (Objects.isNull(referral) ||
                StringUtils.isEmpty(referral.getReferralCode()) ||
                Objects.isNull(referral.getReferralDiscount()) ||
                StringUtils.isEmpty(referral.getEventId()) ||
                Objects.isNull(referral.getReferralType())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Referral existingReferral = referralRepository.findByReferralCode(referral.getReferralCode());

        if (existingReferral != null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Referral code already exists");
        }

        Event selectedEvent = eventRepository.findByEventId(referral.getEventId());

        if (selectedEvent == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event not found");
        }
    }
}
