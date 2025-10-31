package com.web.bookingKol.domain.payment.mappers;

import com.web.bookingKol.domain.booking.mappers.ContractMapper;
import com.web.bookingKol.domain.payment.dtos.refund.RefundDTO;
import com.web.bookingKol.domain.payment.models.Refund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {
    @Autowired
    private ContractMapper contractMapper;

    public RefundDTO toDto(Refund refund) {
        RefundDTO dto = new RefundDTO();
        dto.setId(refund.getId());
        dto.setAmount(refund.getAmount());
        dto.setReason(refund.getReason());
        dto.setStatus(refund.getStatus());
        dto.setRefundedAt(refund.getRefundedAt());
        dto.setCreatedAt(refund.getCreatedAt());
        dto.setContract(contractMapper.toDto(refund.getContract()));
        dto.setBankNumber(refund.getBankNumber());
        dto.setBankName(refund.getBankName());
        return dto;
    }

    public RefundDTO toDtoLighter(Refund refund) {
        RefundDTO dto = new RefundDTO();
        dto.setId(refund.getId());
        dto.setAmount(refund.getAmount());
        dto.setReason(refund.getReason());
        dto.setStatus(refund.getStatus());
        dto.setRefundedAt(refund.getRefundedAt());
        dto.setCreatedAt(refund.getCreatedAt());
        dto.setBankNumber(refund.getBankNumber());
        dto.setBankName(refund.getBankName());
        return dto;
    }
}
