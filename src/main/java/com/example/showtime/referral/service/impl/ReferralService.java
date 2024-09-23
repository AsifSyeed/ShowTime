package com.example.showtime.referral.service.impl;

import com.example.showtime.admin.model.entity.Admin;
import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.repository.EventRepository;
import com.example.showtime.referral.enums.ReferralTypeEnum;
import com.example.showtime.referral.model.request.CreateReferralRequest;
import com.example.showtime.referral.model.entity.Referral;
import com.example.showtime.referral.model.response.GetReferralResponse;
import com.example.showtime.referral.repository.ReferralRepository;
import com.example.showtime.referral.service.IReferralService;
import com.example.showtime.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        return referralRepository.findAll();
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

        return newReferral;
    }

    private void validateRequest(CreateReferralRequest referral) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create a referral");
        }

        if (Objects.isNull(referral) ||
                StringUtils.isEmpty(referral.getReferralCode()) ||
                Objects.isNull(referral.getReferralDiscount()) ||
                StringUtils.isEmpty(referral.getEventId()) ||
                Objects.isNull(referral.getReferralType())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Event selectedEvent = eventRepository.findByEventId(referral.getEventId());

        if (selectedEvent == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event not found");
        }
    }
}
