package com.example.showtime.transaction.repository;

import com.example.showtime.transaction.model.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionItem, Long> {

    TransactionItem findByTransactionRefNo(String transactionRefNo);

    TransactionItem findByUserEmailAndTransactionRefNo(String email, String transactionRefNo);

    List<TransactionItem> findByTransactionStatusOrderByTransactionDate(int value);

    List<TransactionItem>  findByEventIdAndTransactionStatus(String eventId, int value);

    List<TransactionItem> findByEventIdInAndTransactionStatus(List<String> eventIds, int value);
}
