package com.web.bookingKol.domain.booking.services.impl;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.ContractPaymentScheduleResponse;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.services.AdminContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContractServiceImpl implements AdminContractService {

    private final ContractRepository contractRepository;
    private final ContractPaymentScheduleRepository contractPaymentScheduleRepository;

    @Override
    public ApiResponse<PagedResponse<UserContractResponse>> getAllContracts(String keyword, Pageable pageable) {
        String searchKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();

        Page<Contract> page = contractRepository.searchContractsByKeyword(searchKeyword, pageable);

        Page<UserContractResponse> mapped = page.map(contract -> {
            List<ContractPaymentScheduleResponse> payments =
                    contractPaymentScheduleRepository.findByContract_Id(contract.getId())
                            .stream()
                            .map(cps -> ContractPaymentScheduleResponse.builder()
                                    .id(cps.getId())
                                    .contractId(cps.getContract().getId())
                                    .bookingRequestId(cps.getBookingRequest().getId())
                                    .installmentNumber(cps.getInstallmentNumber())
                                    .amount(cps.getAmount())
                                    .dueDate(cps.getDueDate())
                                    .status(cps.getStatus().name())
                                    .build())
                            .collect(Collectors.toList());

            return UserContractResponse.builder()
                    .contractId(contract.getId())
                    .contractNumber(contract.getContractNumber())
                    .status(contract.getStatus())
                    .terms(contract.getTerms())
                    .createdAt(contract.getCreatedAt())
                    .updatedAt(contract.getUpdatedAt())
                    .bookingRequestId(contract.getBookingRequest().getId())
                    .bookingDescription(contract.getBookingRequest().getDescription())
                    .bookingStatus(contract.getBookingRequest().getStatus())
                    .paymentSchedules(payments)
                    .build();
        });

        return ApiResponse.<PagedResponse<UserContractResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách hợp đồng thành công"))
                .data(PagedResponse.fromPage(mapped))
                .build();
    }
}

