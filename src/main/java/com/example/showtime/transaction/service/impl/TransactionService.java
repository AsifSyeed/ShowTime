package com.example.showtime.transaction.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.model.response.CheckTransactionStatusResponse;
import com.example.showtime.transaction.repository.TransactionRepository;
import com.example.showtime.transaction.service.ITransactionService;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final SSLTransactionInitiator sslTransactionInitiator;

    public void saveTransaction(TransactionItem transactionItem) {
        transactionRepository.save(transactionItem);
    }

    @Value("${node.id}")
    private Long nodeId;

    @Override
    public String generateUniqueIdForTransaction(String prefix) {
        return generateUniqueId(prefix);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CheckTransactionStatusResponse checkStatus(CheckTransactionStatusRequest checkTransactionStatusRequest) {
        try {
            TransactionItem transactionItem = validateRequest(checkTransactionStatusRequest);

            if (sslTransactionInitiator.verifySSLTransaction(checkTransactionStatusRequest)) {
                updateTransactionStatus(transactionItem, TransactionStatusEnum.SUCCESS);
            } else {
                updateTransactionStatus(transactionItem, TransactionStatusEnum.FAILED);
            }

            return CheckTransactionStatusResponse.builder()
                    .transactionRefNo(transactionItem.getTransactionRefNo())
                    .transactionStatus(transactionItem.getTransactionStatus())
                    .totalAmount(transactionItem.getTotalAmount())
                    .build();

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private void updateTransactionStatus(TransactionItem transactionItem, TransactionStatusEnum transactionStatus) {
        transactionItem.setTransactionStatus(transactionStatus.getValue());
        transactionRepository.save(transactionItem);
    }

    @Override
    public TransactionItem getTransactionByUserIdAndTransactionRefNo(String email, String transactionRefNo) {
        return transactionRepository.findByUserIdAndTransactionRefNo(email, transactionRefNo);
    }

    private TransactionItem validateRequest(CheckTransactionStatusRequest checkTransactionStatusRequest) {
        if (checkTransactionStatusRequest.getTransactionRefNo() == null || checkTransactionStatusRequest.getTransactionRefNo().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction ID is required");
        }

        if (checkTransactionStatusRequest.getValidationId() == null || checkTransactionStatusRequest.getValidationId().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Validation ID is required");
        }

        if (checkTransactionStatusRequest.getTransactionAmount() == null || checkTransactionStatusRequest.getTransactionAmount().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction Amount ID is required");
        }

        if (checkTransactionStatusRequest.getTransactionCurrency() == null || checkTransactionStatusRequest.getTransactionCurrency().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction Currency ID is required");
        }

        TransactionItem selectedTransaction = transactionRepository.findByTransactionRefNo(checkTransactionStatusRequest.getTransactionRefNo());

        if (selectedTransaction == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Transaction not found");
        }

        return selectedTransaction;
    }

    public String generateUniqueId(String prefix) {
        long timestamp = System.currentTimeMillis();
        long nodeIdMod = timestamp % Long.parseLong(String.valueOf(nodeId)); // Mod nodeId with current milliseconds

        return (prefix + nodeIdMod + "-" + UUID.randomUUID()).toLowerCase(); // Combine prefix, nodeIdMod, and UUID
    }
}
