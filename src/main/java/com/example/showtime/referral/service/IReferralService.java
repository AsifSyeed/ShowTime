package com.example.showtime.referral.service;

import com.example.showtime.referral.model.request.CreateReferralRequest;
import com.example.showtime.referral.model.entity.Referral;
import com.example.showtime.referral.model.response.GetReferralResponse;

import java.util.List;

public interface IReferralService {
    List<GetReferralResponse> getReferralByEventId(String eventId);

    List<Referral> getAllReferral();

    void createReferral(CreateReferralRequest referral);

    Referral getDefaultReferral(String eventId);
}
