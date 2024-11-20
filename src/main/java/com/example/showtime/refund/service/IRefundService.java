package com.example.showtime.refund.service;

import com.example.showtime.refund.model.request.RefundRequest;
import com.example.showtime.refund.model.response.RefundResponse;

public interface IRefundService {
    RefundResponse refundTransaction(RefundRequest refundRequest);
}
