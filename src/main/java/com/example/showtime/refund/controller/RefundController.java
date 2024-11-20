package com.example.showtime.refund.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.refund.model.response.RefundResponse;
import com.example.showtime.refund.service.IRefundService;
import com.example.showtime.refund.model.request.RefundRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refund")
public class RefundController {

    private final IRefundService refundService;

    @PostMapping("/admin/initiate")
    public ResponseEntity<ApiResponse<RefundResponse>> refundTransaction(@RequestBody RefundRequest refundRequest) {
        RefundResponse response = refundService.refundTransaction(refundRequest);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Operation Successful", response));
    }
}
