package com.example.showtime.transaction.service.impl;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;

    public void saveTransaction(TransactionItem transactionItem) {
        transactionRepository.save(transactionItem);
    }

    @Value("${node.id}")
    private Long nodeId;

    @Override
    public String generateUniqueIdForTransaction(String prefix) {
        return generateUniqueId(prefix);
    }

    public String generateUniqueId(String prefix) {
        long timestamp = System.currentTimeMillis();
        long nodeIdMod = timestamp % Long.parseLong(String.valueOf(nodeId)); // Mod nodeId with current milliseconds

        return (prefix + nodeIdMod + "-" + UUID.randomUUID()).toLowerCase(); // Combine prefix, nodeIdMod, and UUID
    }
}
