package com.web.bookingKol.domain.payment.mappers;

import com.web.bookingKol.domain.payment.dtos.PaymentDTO;
import com.web.bookingKol.domain.payment.models.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDTO toDto(Payment payment);

    Payment toEntity(PaymentDTO paymentDTO);
}
