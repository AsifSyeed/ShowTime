package com.example.showtime.referral.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.referral.model.request.CreateReferralRequest;
import com.example.showtime.referral.model.entity.Referral;
import com.example.showtime.referral.model.request.UpdateReferralRequest;
import com.example.showtime.referral.model.response.GetReferralResponse;
import com.example.showtime.referral.service.IReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://www.countersbd.com", "https://admin.countersbd.com", "https://checker.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/referral")
public class ReferralController {

    private final IReferralService referralService;

    // create an api to get all referrals
    @GetMapping("/admin-all")
    public ResponseEntity<ApiResponse<List<Referral>>> getAllReferrals() {
        List<Referral> referralResponse = referralService.getAllReferral();

        ApiResponse<List<Referral>> response = new ApiResponse<>(HttpStatus.OK.value(), "Referral retrieved successfully", referralResponse);

        return ResponseEntity.ok(response);
    }

    // create an api to get all referrals by event id
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetReferralResponse>>> getReferralByEventId(@RequestParam String eventId) {
        List<GetReferralResponse> referralResponse = referralService.getReferralByEventId(eventId);

        ApiResponse<List<GetReferralResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Referral retrieved successfully", referralResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createReferral(@RequestBody CreateReferralRequest referral) {
        referralService.createReferral(referral);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Referral created successfully", null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateReferral(@RequestBody UpdateReferralRequest referral) {
        referralService.updateReferral(referral);

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Referral updated successfully", null);

        return ResponseEntity.ok(response);
    }
}
