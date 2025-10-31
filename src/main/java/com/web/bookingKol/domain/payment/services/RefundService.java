package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.payment.dtos.refund.RefundDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RefundService {
    RefundDTO createRefundRequest(Contract contract, String bankNumber, String bankName);

    ApiResponse<RefundDTO> confirmRefunded(UUID refundId);

    Page<RefundDTO> getRefunds(Pageable pageable, String status, UUID contractId);

    ApiResponse<RefundDTO> detailRefund(UUID refundId);
}
