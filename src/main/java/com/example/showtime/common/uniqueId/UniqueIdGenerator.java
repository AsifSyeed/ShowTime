package com.example.showtime.common.uniqueId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UniqueIdGenerator {
    private static final int SEQUENCE_BITS = 12;
    private static final int MAX_SEQUENCE = (int) (Math.pow(2, SEQUENCE_BITS) - 1);

    private final AtomicInteger sequence = new AtomicInteger(0);
    private long lastTimestamp = -1L;

    @Value("${node.id}")
    private Long nodeId;

    public synchronized String generateUniqueId(String prefix) {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            int sequenceValue = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (sequenceValue == 0) {
                timestamp = tilNextMillis(lastTimestamp); // Move to the next millisecond if sequence overflows
            }
        } else {
            sequence.set(0); // Reset sequence when timestamp changes
        }

        lastTimestamp = timestamp;

        return String.format("%s-%d-%04d", prefix, timestamp, sequence.get()); // Format with 4 digits for sequence
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public String generateUniqueUUID(String prefix) {
        long timestamp = System.currentTimeMillis();
        long nodeIdMod = timestamp % Long.parseLong(String.valueOf(nodeId)); // Mod nodeId with current milliseconds

        return (prefix + nodeIdMod + "-" + UUID.randomUUID()).toLowerCase(); // Combine prefix, nodeIdMod, and UUID
    }

    //Write a function to generate a 6 digit OTP
    public String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
