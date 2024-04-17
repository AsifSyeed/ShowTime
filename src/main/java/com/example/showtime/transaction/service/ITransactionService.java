package com.example.showtime.transaction.service;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;

public interface ITransactionService {
    void saveTransaction(TransactionItem transactionItem);

    String generateUniqueIdForTransaction(String prefix);

    CheckTransactionStatusResponse checkStatus(CheckTransactionStatusRequest checkTransactionStatusRequest);

    TransactionItem getTransactionByUserEmailAndTransactionRefNo(String email, String transactionRefNo);
}
