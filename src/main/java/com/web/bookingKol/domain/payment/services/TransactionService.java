package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionReportDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public interface TransactionService {
    ApiResponse<TransactionReportDTO> getAllTransactions(Instant startDate, Instant endDate, String status, Pageable pageable);

    ApiResponse<TransactionResponseDTO> getDetailTransaction(Integer transactionId);
}