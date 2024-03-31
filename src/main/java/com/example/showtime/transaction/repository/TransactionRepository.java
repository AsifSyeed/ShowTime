package com.example.showtime.transaction.repository;

import com.example.showtime.transaction.model.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionItem, Long> {

    TransactionItem findByTransactionRefNo(String transactionRefNo);
}
