package com.example.showtime.transaction.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final SSLTransactionInitiator sslTransactionInitiator;
    private final IEventService eventService;
    private final ITicketService ticketService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CheckTransactionStatusResponse checkStatus(CheckTransactionStatusRequest checkTransactionStatusRequest) {
        try {
            TransactionItem transactionItem = validateRequest(checkTransactionStatusRequest);

            return CheckTransactionStatusResponse.builder()
                    .transactionRefNo(transactionItem.getTransactionRefNo())
                    .transactionStatus(transactionItem.getTransactionStatus())
                    .totalAmount(transactionItem.getTotalAmount())
                    .eventName(eventService.getEventById(transactionItem.getEventId()).getEventName())
                    .numberOfTickets(transactionItem.getNumberOfTickets())
                    .build();

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public TransactionItem getTransactionByUserEmailAndTransactionRefNo(String email, String transactionRefNo) {
        return transactionRepository.findByUserEmailAndTransactionRefNo(email, transactionRefNo);
    }

    @Override
    public void sslTransactionUpdate(String transactionRefNo, String validationId, String amount, String currency, String status, String error, String bank_tran_id) {

        if (transactionRefNo == null || transactionRefNo.isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction ID is required");
        }

        if (currency == null || currency.isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Currency is required");
        }

        boolean sslStatusFromRedirectUrl = status.equals("VALID") || status.equals("VALIDATED");
        boolean sslStatus = sslTransactionInitiator.verifySSLTransaction(transactionRefNo, validationId, amount, currency);
        List<Ticket> tickets = ticketService.getTicketListByTransactionRefNo(transactionRefNo);

        TransactionItem transactionItem = new TransactionItem();
        transactionItem.setTransactionRefNo(transactionRefNo);
        transactionItem.setTotalAmount(Double.valueOf(amount));
        transactionItem.setEventId(tickets.get(0).getEventId());
        transactionItem.setTransactionDate(Calendar.getInstance().getTime());
        transactionItem.setUserEmail(tickets.get(0).getTicketCreatedBy());
        transactionItem.setNumberOfTickets(tickets.size());
        transactionItem.setBankTranId(bank_tran_id);
        transactionItem.setErrorMessage(error);
        transactionItem.setValidationId(validationId);

        if (sslStatus == sslStatusFromRedirectUrl) {
            if (sslStatus) {
                transactionItem.setTransactionStatus(TransactionStatusEnum.SUCCESS.getValue());
                updateTicketsStatus(tickets, TransactionStatusEnum.SUCCESS.getValue());
            } else {
                transactionItem.setTransactionStatus(TransactionStatusEnum.FAILED.getValue());
                updateTicketsStatus(tickets, TransactionStatusEnum.FAILED.getValue());
            }

            transactionRepository.save(transactionItem);
        } else {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction data mismatched");
        }
    }

    private void updateTicketsStatus(List<Ticket> tickets, int transactionStatus) {
        ticketService.updateTicketStatus(tickets, transactionStatus);
    }

    private TransactionItem validateRequest(CheckTransactionStatusRequest checkTransactionStatusRequest) {
        if (checkTransactionStatusRequest.getTransactionRefNo() == null || checkTransactionStatusRequest.getTransactionRefNo().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction ID is required");
        }

        TransactionItem selectedTransaction = transactionRepository.findByTransactionRefNo(checkTransactionStatusRequest.getTransactionRefNo());

        if (selectedTransaction == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Transaction not found");
        }

        return selectedTransaction;
    }
}
