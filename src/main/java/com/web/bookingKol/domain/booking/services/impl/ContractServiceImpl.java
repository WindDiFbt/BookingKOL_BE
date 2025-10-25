package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.NumberGenerateUtil;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.services.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ContractServiceImpl implements ContractService {
    @Autowired
    private ContractRepository contractRepository;

    @Override
    public Contract createNewContract(BookingRequest bookingRequest) {
        Contract contract = new Contract();
        String code;
        do {
            code = NumberGenerateUtil.generateSecureRandomContractNumber();
        } while (contractRepository.existsByContractNumber(code));
        contract.setContractNumber(code);
        contract.setBookingRequest(bookingRequest);
        contract.setStatus(Enums.ContractStatus.SIGNED.name());
        contract.setCreatedAt(Instant.now());
        contract.setAmount(bookingRequest.getKol().getMinBookingPrice());
        contractRepository.save(contract);
        return contract;
    }
}
