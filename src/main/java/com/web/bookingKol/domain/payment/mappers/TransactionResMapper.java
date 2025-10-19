package com.web.bookingKol.domain.payment.mappers;

import com.web.bookingKol.domain.payment.dtos.transaction.TransactionResponseDTO;
import com.web.bookingKol.domain.payment.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionResMapper {
    public TransactionResponseDTO toDto(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setGateway(transaction.getGateway());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setAccountNumber(transaction.getAccountNumber());
        dto.setSubAccount(transaction.getSubAccount());
        dto.setAmountIn(transaction.getAmountIn());
        dto.setAmountOut(transaction.getAmountOut());
        dto.setAccumulated(transaction.getAccumulated());
        dto.setCode(transaction.getCode());
        dto.setTransactionContent(transaction.getTransactionContent());
        dto.setReferenceNumber(transaction.getReferenceNumber());
        dto.setBody(transaction.getBody());
        dto.setCreatedAt(transaction.getCreatedAt());
        if (transaction.getPayment() != null) {
            dto.setPaymentId(transaction.getPayment().getId());
        }
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}
