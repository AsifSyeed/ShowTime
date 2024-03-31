package com.example.showtime.transaction.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
public class TransactionController {
    private final ITransactionService transactionService;

    @PostMapping("/check-status")
    public ResponseEntity<ApiResponse<CheckTransactionStatusResponse>> checkStatus(@RequestBody @Valid CheckTransactionStatusRequest checkTransactionStatusRequest) {
        ApiResponse<CheckTransactionStatusResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Operation successful", transactionService.checkStatus(checkTransactionStatusRequest));

        return ResponseEntity.ok(response);
    }
}
