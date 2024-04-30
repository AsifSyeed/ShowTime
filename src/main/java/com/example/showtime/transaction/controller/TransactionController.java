package com.example.showtime.transaction.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@CrossOrigin(origins = "*", maxAge = 3600)
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

    @PostMapping("/ssl-redirect")
    public ResponseEntity<ApiResponse<?>> sslRedirect(@RequestParam String val_id, String tran_id, String amount, String bank_tran_id, String currency) {

        transactionService.sslTransactionUpdate(tran_id, val_id, amount, currency);

        String frontendUrl = "http://localhost:3000/checkout/validate?&tran_id=" + tran_id;
        URI redirectUri = ServletUriComponentsBuilder.fromUriString(frontendUrl).build().toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
}
