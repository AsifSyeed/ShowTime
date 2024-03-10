package com.example.showtime.transaction.service.impl;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static jdk.jfr.internal.handlers.EventHandler.timestamp;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;

    private static final int NODE_ID_BITS = 10;
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

    private synchronized String generateUniqueId(String prefix) {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp)
            throw new IllegalStateException("Bad System Clock!");

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0)
                currentTimestamp = waitNextMillis(currentTimestamp);

        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS);
        id |= ((long) nodeId << SEQUENCE_BITS);
        id |= sequence;
        return prefix.concat(String.valueOf(id));
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }
}
