package com.example.showtime.transaction.service.impl;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;

    private static final int SEQUENCE_BITS = 12;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;
    private static final int MAX_SEQUENCE = (int) (Math.pow(2, SEQUENCE_BITS) - 1);

    private int nodeId;

    public void saveTransaction(TransactionItem transactionItem) {
        transactionRepository.save(transactionItem);
    }

    @Override
    public String generateUniqueIdForTransaction(String prefix) {
        return generateUniqueId(prefix);
    }

    private String generateUniqueId(String prefix) {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return String.format("%s-%d-%d-%d", prefix, timestamp, nodeId, sequence);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
