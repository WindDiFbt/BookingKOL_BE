package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionReportDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionResponseDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionStatsDTO;
import com.web.bookingKol.domain.payment.mappers.TransactionResMapper;
import com.web.bookingKol.domain.payment.models.Transaction;
import com.web.bookingKol.domain.payment.repositories.TransactionRepository;
import com.web.bookingKol.domain.payment.services.TransactionService;
import com.web.bookingKol.domain.payment.spec.TransactionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionResMapper transactionResMapper;

    @Override
    public ApiResponse<TransactionReportDTO> getAllTransactions(Instant startDate, Instant endDate, String status, Pageable pageable) {
        if (startDate == null && endDate == null) {
            endDate = Instant.now();
            startDate = endDate.minus(30, ChronoUnit.DAYS);
        } else if (startDate != null && endDate == null) {
            endDate = Instant.now();
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        List<TransactionRepository.TransactionStatsProjection> statsProjections =
                transactionRepository.getTransactionStats(startDate, endDate);
        long completed = 0;
        long failed = 0;
        long orphaned = 0;
        long pending = 0;
        BigDecimal totalAmountIn = BigDecimal.ZERO;
        for (var proj : statsProjections) {
            if (proj.getStatus() != null) {
                switch (proj.getStatus()) {
                    case "COMPLETED":
                        completed = proj.getCount();
                        break;
                    case "FAILED":
                        failed = proj.getCount();
                        break;
                    case "ORPHANED":
                        orphaned = proj.getCount();
                        break;
                    case "PENDING":
                        pending = proj.getCount();
                        break;
                }
            }
            if (proj.getTotalAmountIn() != null) {
                totalAmountIn = totalAmountIn.add(proj.getTotalAmountIn());
            }
        }
        TransactionStatsDTO stats = TransactionStatsDTO.builder()
                .completedCount(completed)
                .failedCount(failed)
                .orphanedCount(orphaned)
                .totalTransactions(completed + failed + orphaned + pending)
                .totalAmountIn(totalAmountIn)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        Specification<Transaction> spec = TransactionSpecification.filterBy(startDate, endDate, status);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
        Page<TransactionResponseDTO> transactionDtoPage = transactionPage.map(transactionResMapper::toDto);
        TransactionReportDTO report = TransactionReportDTO.builder()
                .stats(stats)
                .transactions(transactionDtoPage)
                .build();
        return ApiResponse.<TransactionReportDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Transaction report retrieved successfully"))
                .data(report)
                .build();
    }

    @Override
    public ApiResponse<TransactionResponseDTO> getDetailTransaction(Integer transactionId) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            return ApiResponse.<TransactionResponseDTO>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("Transaction not found with id: " + transactionId))
                    .data(null)
                    .build();
        }
        Transaction transaction = optionalTransaction.get();
        TransactionResponseDTO dto = transactionResMapper.toDto(transaction);
        return ApiResponse.<TransactionResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Transaction details retrieved successfully"))
                .data(dto)
                .build();
    }
}
