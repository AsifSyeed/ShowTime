package com.example.showtime.common.uniqueId;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueIdGenerator {
    private static final int SEQUENCE_BITS = 12;
    private static final int MAX_SEQUENCE = (int) (Math.pow(2, SEQUENCE_BITS) - 1);

    private final AtomicInteger sequence = new AtomicInteger(0);
    private long lastTimestamp = -1L;

    private final long nodeId;

    public UniqueIdGenerator(long nodeId) {
        this.nodeId = nodeId;
    }


    public synchronized String generateUniqueId(String prefix) {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            int sequenceValue = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (sequenceValue == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        return String.format("%s-%d-%d", prefix, timestamp, sequence.get());
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public String generateUniqueTransactionReferenceNo(String prefix) {
        long timestamp = System.currentTimeMillis();
        long nodeIdMod = timestamp % Long.parseLong(String.valueOf(nodeId)); // Mod nodeId with current milliseconds

        return (prefix + nodeIdMod + "-" + UUID.randomUUID()).toLowerCase(); // Combine prefix, nodeIdMod, and UUID
    }
}
