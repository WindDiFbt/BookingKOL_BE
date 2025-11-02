package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.payment.dtos.refund.RefundDTO;
import com.web.bookingKol.domain.payment.mappers.RefundMapper;
import com.web.bookingKol.domain.payment.models.Refund;
import com.web.bookingKol.domain.payment.repositories.RefundRepository;
import com.web.bookingKol.domain.payment.services.RefundService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefundServiceImpl implements RefundService {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private RefundMapper refundMapper;

    @Override
    public RefundDTO createRefundRequest(Contract contract, String bankNumber, String bankName) {
        BookingRequest bookingRequest = contract.getBookingRequest();
        if (!contract.getStatus().equals(Enums.ContractStatus.PAID.name())) {
            throw new IllegalArgumentException("Hợp đồng đã hủy hoặc đã hoàn thành. Không thể tạo yêu cầu hoàn tiền.");
        }
        Instant cancellationTime = Instant.now();
        Instant bookingStartTime = bookingRequest.getStartAt();
        BigDecimal totalAmount = contract.getAmount();
        BigDecimal refundAmount;
        String refundReason;
        Duration timeRemaining = Duration.between(cancellationTime, bookingStartTime);
        Duration twentyFourHours = Duration.ofHours(24);

        if (timeRemaining.isNegative()) {
            throw new IllegalArgumentException("Thời gian hủy không hợp lệ.");
        } else if (timeRemaining.compareTo(twentyFourHours) > 0) {
            BigDecimal percentage = new BigDecimal("0.80");
            refundAmount = totalAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
            refundReason = "Hủy trước 24 giờ. Phí phạt 20%.";
        } else {
            BigDecimal percentage = new BigDecimal("0.50");
            refundAmount = totalAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
            refundReason = "Hủy trong vòng 24 giờ. Phí phạt 50%.";
        }
        Refund refund = new Refund();
        refund.setId(UUID.randomUUID());
        refund.setAmount(refundAmount);
        refund.setReason(refundReason);
        refund.setStatus(Enums.RefundStatus.PENDING.name());
        refund.setCreatedAt(Instant.now());
        refund.setContract(contract);
        refund.setBankNumber(bankNumber);
        refund.setBankName(bankName);
        refundRepository.save(refund);
        return refundMapper.toDto(refund);
    }

    @Override
    public ApiResponse<RefundDTO> confirmRefunded(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền với ID: " + refundId));
        if (!refund.getStatus().equals(Enums.RefundStatus.PENDING.name())) {
            throw new IllegalArgumentException("Yêu cầu hoàn tiền đã được xác nhận trước đó.");
        }
        Contract contract = refund.getContract();

        refund.setStatus(Enums.RefundStatus.REFUNDED.name());
        refund.setRefundedAt(Instant.now());
        refundRepository.save(refund);

        contract.setStatus(Enums.ContractStatus.REFUNDED.name());
        contractRepository.save(contract);
        return ApiResponse.<RefundDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hoàn tiền đã được xác nhận thành công."))
                .data(refundMapper.toDto(refund))
                .build();
    }

    @Override
    public Page<RefundDTO> getRefunds(Pageable pageable, String status, UUID contractId) {
        Page<Refund> refundEntityPage = refundRepository.findByStatusAndContractIdDynamic(
                status,
                contractId,
                pageable);
        return refundEntityPage.map(refundMapper::toDtoLighter);
    }

    @Override
    public ApiResponse<RefundDTO> detailRefund(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu hoàn tiền với ID: " + refundId));
        RefundDTO refundDTO = refundMapper.toDto(refund);
        return ApiResponse.<RefundDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết yêu cầu hoàn tiền thành công."))
                .data(refundDTO)
                .build();
    }
}
