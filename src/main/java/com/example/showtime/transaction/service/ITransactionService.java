package com.example.showtime.transaction.service;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.model.response.ValidTransactionItems;

import java.util.List;

public interface ITransactionService {

    CheckTransactionStatusResponse checkStatus(CheckTransactionStatusRequest checkTransactionStatusRequest);

    TransactionItem getTransactionByUserEmailAndTransactionRefNo(String email, String transactionRefNo);

    void sslTransactionUpdate(String transactionRefNo, String validationId, String amount, String currency, String status, String error, String bank_tran_id);

    List<ValidTransactionItems> getValidTransactions();

    List<TransactionItem> getTransactionsByEventIdAndTransactionStatus(String eventId, int value);

    List<TransactionItem> getTransactionsByTransactionStatus(int value);

    List<TransactionItem> getTransactionsByEventIdsAndTransactionStatus(List<String> eventIds, int value);
}
