package com.example.showtime.transaction.service;

import com.example.showtime.transaction.model.entity.TransactionItem;

public interface ITransactionService {
    void saveTransaction(TransactionItem transactionItem);

    String generateUniqueIdForTransaction(String prefix);
}
