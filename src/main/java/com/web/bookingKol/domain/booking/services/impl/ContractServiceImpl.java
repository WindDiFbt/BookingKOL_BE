package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.NumberGenerateUtil;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.services.ContractService;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.services.impl.ContractGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ContractServiceImpl implements ContractService {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractGeneratorService contractGeneratorService;

    @Override
    public Contract createNewContract(BookingRequest bookingRequest) {
        Contract contract = new Contract();
        String code;
        do {
            code = NumberGenerateUtil.generateSecureRandomContractNumber();
        } while (contractRepository.existsByContractNumber(code));
        contract.setContractNumber(code);
        contract.setBookingRequest(bookingRequest);
        User user = bookingRequest.getUser();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        contract.setStatus(Enums.ContractStatus.DRAFT.name());
        contract.setCreatedAt(Instant.now());
        BigDecimal durationBig = BigDecimal.valueOf(
                Duration.between(bookingRequest.getStartAt(), bookingRequest.getEndAt()).toMinutes() / 60.0
        );
        BigDecimal totalAmount = bookingRequest.getKol().getMinBookingPrice().multiply(durationBig);
        contract.setAmount(totalAmount);
        contractRepository.saveAndFlush(contract);
        var placeholders = Map.of(
                "username", user.getFullName(),
                "user_address", user.getAddress(),
                "phone_number", user.getPhone(),
                "email", user.getEmail(),
                "platform", bookingRequest.getPlatform(),
                "startAt", fmt.format(bookingRequest.getStartAt()),
                "endAt", fmt.format(bookingRequest.getEndAt()),
                "location", bookingRequest.getLocation(),
                "kol_name", bookingRequest.getKol().getUser().getFullName(),
                "amount", bookingRequest.getKol().getDisplayName()
        );
        FileUsageDTO fileUsageDTO = contractGeneratorService.generateAndSaveContractForSingle(placeholders, user.getId(), contract.getId());
        contract.setTerms("File hợp đồng: " + fileUsageDTO.getFile().getFileUrl());
        contractRepository.save(contract);
        return contract;
    }

    @Override
    public void confirmContract(Contract contract) {
        contract.setStatus(Enums.ContractStatus.SIGNED.name());
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
    }

    @Override
    public void cancelContract(Contract contract) {
        contract.setStatus(Enums.ContractStatus.CANCELLED.name());
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
    }

    @Override
    public void paidContract(Contract contract) {
        contract.setStatus(Enums.ContractStatus.PAID.name());
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
    }
}
