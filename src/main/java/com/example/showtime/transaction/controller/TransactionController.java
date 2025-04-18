package com.example.showtime.transaction.controller;

import com.example.showtime.common.model.response.ApiResponse;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.refund.model.request.RefundRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.model.response.ValidTransactionItems;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
public class TransactionController {
    private final ITransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @Value("${sslcommerz.redirect.web.url}")
    private String redirectWebUrl;

    @PostMapping("/check-status")
    public ResponseEntity<ApiResponse<CheckTransactionStatusResponse>> checkStatus(@RequestBody @Valid CheckTransactionStatusRequest checkTransactionStatusRequest) {
        ApiResponse<CheckTransactionStatusResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Operation successful", transactionService.checkStatus(checkTransactionStatusRequest));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ssl-redirect")
    public ResponseEntity<ApiResponse<?>> sslRedirect(@RequestParam String tran_id,
                                                      @RequestParam(required = false) String val_id,
                                                      @RequestParam(required = false) String amount,
                                                      @RequestParam(required = false) String bank_tran_id,
                                                      @RequestParam(required = false) String currency,
                                                      @RequestParam(required = false) String error,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String card_type,
                                                      @RequestParam(required = false) String card_issuer,
                                                      @RequestParam(required = false) String card_brand) {

        transactionService.sslTransactionUpdate(tran_id, val_id, amount, currency, status, error, bank_tran_id, card_type, card_issuer, card_brand);

        String frontendUrl = redirectWebUrl + "/checkout/validate?&tran_id=" + tran_id;
        URI redirectUri = ServletUriComponentsBuilder.fromUriString(frontendUrl).build().toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/admin/valid-transactions")
    public ResponseEntity<ApiResponse<List<ValidTransactionItems>>> getValidTransactions() {
        List<ValidTransactionItems> response = transactionService.getValidTransactions();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Valid transactions retrieved successfully", response));
    }
}
