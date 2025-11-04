package com.web.bookingKol.domain.booking.services.impl;


import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.ContractPaymentScheduleResponse;
import com.web.bookingKol.domain.booking.dtos.CreatePaymentScheduleRequest;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;

import com.web.bookingKol.domain.booking.services.ContractPaymentScheduleService;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractPaymentScheduleServiceImpl implements ContractPaymentScheduleService {

    private final ContractRepository contractRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ContractPaymentScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ContractPaymentScheduleRepository contractPaymentScheduleRepository;

    @Override
    @Transactional
    public ApiResponse<?> createPaymentSchedule(CreatePaymentScheduleRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        BookingRequest bookingRequest = bookingRequestRepository.findById(request.getBookingRequestId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking request"));

        if (!Enums.ContractStatus.SIGNED.name().equalsIgnoreCase(contract.getStatus())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Chỉ có thể chia đợt thanh toán khi hợp đồng ở trạng thái SIGNED"))
                    .data(null)
                    .build();
        }

        BigDecimal totalFromInstallments = request.getInstallments().stream()
                .map(i -> i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal contractAmount = contract.getAmount() != null ? contract.getAmount() : BigDecimal.ZERO;
        if (totalFromInstallments.compareTo(contractAmount) != 0) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of(String.format(
                            "Tổng tiền các đợt (%,.0f) phải bằng tổng hợp đồng (%,.0f)",
                            totalFromInstallments, contractAmount
                    )))
                    .data(null)
                    .build();
        }

        scheduleRepository.findByContract_Id(contract.getId())
                .forEach(scheduleRepository::delete);

        List<ContractPaymentScheduleResponse> result = new ArrayList<>();
        int i = 1;

        for (CreatePaymentScheduleRequest.InstallmentItem item : request.getInstallments()) {
            ContractPaymentSchedule schedule = new ContractPaymentSchedule();
            schedule.setContract(contract);
            schedule.setBookingRequest(bookingRequest);
            schedule.setInstallmentNumber(i++);
            schedule.setAmount(item.getAmount());
            schedule.setDueDate(item.getDueDate());
            schedule.setStatus(Enums.PaymentScheduleStatus.PENDING);
            schedule.setCreatedAt(Instant.now());
            schedule.setUpdatedAt(Instant.now());

            ContractPaymentSchedule saved = scheduleRepository.save(schedule);

            result.add(ContractPaymentScheduleResponse.builder()
                    .id(saved.getId())
                    .contractId(contract.getId())
                    .bookingRequestId(bookingRequest.getId())
                    .installmentNumber(saved.getInstallmentNumber())
                    .amount(saved.getAmount())
                    .dueDate(saved.getDueDate())
                    .status(saved.getStatus().name())
                    .build());
        }

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo kế hoạch thanh toán thành công"))
                .data(result)
                .build();
    }





}

