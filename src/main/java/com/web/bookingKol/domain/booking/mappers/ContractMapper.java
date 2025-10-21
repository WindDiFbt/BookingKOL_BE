package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.domain.booking.dtos.ContractDTO;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.payment.mappers.PaymentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {PaymentMapper.class})
public interface ContractMapper {
    @Mapping(target = "paymentDTO", source = "payment")
    ContractDTO toDto(Contract contract);

    @Mapping(target = "paymentDTO", source = "payment")
    Set<ContractDTO> toDtoSet(Set<Contract> contracts);
}
