package com.example.showtime.refund.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.refund.enums.RefundStatusEnum;
import com.example.showtime.refund.model.entity.RefundItem;
import com.example.showtime.refund.model.response.RefundResponse;
import com.example.showtime.refund.repository.RefundRepository;
import com.example.showtime.refund.service.IRefundService;
import com.example.showtime.refund.model.request.RefundRequest;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.response.SSLRefundResponse;
import com.example.showtime.transaction.service.ITransactionService;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService implements IRefundService {

    private final ITicketService ticketService;
    private final ITransactionService transactionService;
    private final SSLTransactionInitiator sslTransactionInitiator;
    private final RefundRepository refundRepository;

    @Override
    public RefundResponse refundTransaction(RefundRequest refundRequest) {
        TransactionItem transactionItem = validateRefundRequest(refundRequest);

        if (transactionItem.getTransactionStatus() != TransactionStatusEnum.SUCCESS.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction is not successful");
        }

        List<Ticket> tickets = ticketService.getTicketListByTransactionRefNo(transactionItem.getTransactionRefNo());

        if (tickets.isEmpty()) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Invalid transaction ID");
        }


        SSLRefundResponse sslRefundResponse = sslTransactionInitiator.refundTransaction(transactionItem.getBankTranId(), transactionItem.getTotalAmount().toString(), "Refund", transactionItem.getTransactionRefNo());

        if (sslRefundResponse == null) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Refund failed");
        }

        if (sslRefundResponse.getStatus().equals("FAILED")) {
            throw new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Refund failed");
        }

        RefundItem refundItem = new RefundItem();

        refundItem.setTransactionRefNo(transactionItem.getTransactionRefNo());
        refundItem.setRefundAmount(transactionItem.getTotalAmount());
        refundItem.setRefundStatus(RefundStatusEnum.fromString(sslRefundResponse.getStatus()).getValue());
        refundItem.setRefundDate(Calendar.getInstance().getTime());
        refundItem.setRefundRefId(sslRefundResponse.getRefundRefId());
        refundItem.setEventId(transactionItem.getEventId());
        refundItem.setBankTranId(transactionItem.getBankTranId());
        refundItem.setErrorMessage(sslRefundResponse.getErrorReason());
        refundItem.setUserEmail(transactionItem.getUserEmail());

        refundRepository.save(refundItem);

        if (refundItem.getRefundStatus() == RefundStatusEnum.SUCCESS.getValue()) {
            updateTicketsStatus(tickets, TransactionStatusEnum.REFUNDED.getValue());
            transactionItem.setTransactionStatus(TransactionStatusEnum.REFUNDED.getValue());
            transactionService.updateTransaction(transactionItem);
        }

        return RefundResponse.builder()
                .transactionId(refundItem.getTransactionRefNo())
                .refundId(refundItem.getRefundRefId())
                .refundStatus(RefundStatusEnum.getStringValueFromInt(refundItem.getRefundStatus()))
                .errorMessage(refundItem.getErrorMessage())
                .build();
    }

    private void updateTicketsStatus(List<Ticket> tickets, int value) {
        ticketService.updateTicketStatus(tickets, value);
    }

    private TransactionItem validateRefundRequest(RefundRequest refundRequest) {
        if (refundRequest == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid request");
        }

        if (refundRequest.getTransactionRefNo() == null || refundRequest.getTransactionRefNo().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction reference number is required");
        }

        if (refundRequest.getRemarks() == null || refundRequest.getRemarks().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Remarks is required");
        }

        TransactionItem selectedTransaction = transactionService.getTransactionByTransactionRefNo(refundRequest.getTransactionRefNo());

        if (selectedTransaction == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Transaction not found");
        }

        if (selectedTransaction.getTransactionStatus() != TransactionStatusEnum.SUCCESS.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction is not successful");
        }

        return selectedTransaction;
    }
}
