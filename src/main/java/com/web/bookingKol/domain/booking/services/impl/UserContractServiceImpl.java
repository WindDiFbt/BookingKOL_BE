package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.ContractPaymentScheduleResponse;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.services.UserContractService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserContractServiceImpl implements UserContractService {

    private final ContractRepository contractRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;
    private final ContractPaymentScheduleRepository contractPaymentScheduleRepository;

    @Override
    @Transactional
    public ApiResponse<?> signContract(UUID contractId, UUID bookingRequestId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + contractId));

        BookingRequest booking = contract.getBookingRequest();
        if (!booking.getId().equals(bookingRequestId)) {
            throw new RuntimeException("Hợp đồng không thuộc booking request này");
        }

        User owner = booking.getUser();
        if (!owner.getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền ký hợp đồng này");
        }

        if (!Enums.ContractStatus.DRAFT.name().equalsIgnoreCase(contract.getStatus())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Chỉ có thể chấp nhận hợp đồng khi đang ở trạng thái DRAFT"))
                    .data(null)
                    .build();
        }


        contract.setStatus(Enums.ContractStatus.SIGNED.name());
        contract.setUpdatedAt(Instant.now());
        contract.setSignedAtBrand(Instant.now());
        contractRepository.save(contract);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Chấp nhận hợp đồng thành công"))
                .data(new Object() {
                    public final UUID contractId = contract.getId();
                    public final String newStatus = contract.getStatus();
                    public final Instant signedAt = contract.getSignedAtBrand();
                    public final String signedBy = user.getEmail();
                })
                .build();
    }


    // từ chối nhận hợp đồng
    @Override
    @Transactional
    public ApiResponse<?> rejectContract(UUID contractId, UUID bookingRequestId, String userEmail, String reason) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + contractId));

        BookingRequest booking = contract.getBookingRequest();
        if (!booking.getId().equals(bookingRequestId)) {
            throw new RuntimeException("Hợp đồng không thuộc booking request này");
        }

        User owner = booking.getUser();
        if (!owner.getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền từ chối hợp đồng này");
        }

        if (!Enums.ContractStatus.DRAFT.name().equalsIgnoreCase(contract.getStatus())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Chỉ có thể từ chối hợp đồng khi đang ở trạng thái DRAFT"))
                    .data(null)
                    .build();
        }


        contract.setStatus(Enums.ContractStatus.REJECT.name());
        contract.setUpdatedAt(Instant.now());
        contract.setReason(reason);

        contractRepository.save(contract);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Từ chối hợp đồng thành công"))
                .data(new Object() {
                    public final UUID contractId = contract.getId();
                    public final String newStatus = contract.getStatus();
                    public final String rejectedBy = user.getEmail();
                    public final String reason = contract.getReason();
                    public final Instant updatedAt = contract.getUpdatedAt();
                })
                .build();
    }


    @Override
    public ApiResponse<PagedResponse<UserContractResponse>> getUserContracts(String userEmail, String keyword, Pageable pageable) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        String searchKeyword = (keyword == null) ? "" : keyword.trim();

        Page<BookingRequest> page = bookingRequestRepository.searchByUserAndKeyword(user.getId(), searchKeyword, pageable);

        Page<UserContractResponse> mapped = page.map(booking -> {
            Contract latestContract = booking.getContracts().stream()
                    .max(Comparator.comparing(Contract::getCreatedAt))
                    .orElse(null);

            List<ContractPaymentScheduleResponse> payments =
                    latestContract != null
                            ? contractPaymentScheduleRepository.findByContract_Id(latestContract.getId())
                            .stream()
                            .map(cps -> ContractPaymentScheduleResponse.builder()
                                    .id(cps.getId())
                                    .contractId(cps.getContract().getId())
                                    .bookingRequestId(cps.getBookingRequest().getId())
                                    .installmentNumber(cps.getInstallmentNumber())
                                    .amount(cps.getAmount())
                                    .dueDate(cps.getDueDate())
                                    .status(cps.getStatus().name())
                                    .createdAt(cps.getCreatedAt())
                                    .updatedAt(cps.getUpdatedAt())
                                    .build())
                            .toList()
                            : List.of();

            return UserContractResponse.builder()
                    .contractId(latestContract != null ? latestContract.getId() : null)
                    .contractNumber(latestContract != null ? latestContract.getContractNumber() : null)
                    .status(latestContract != null ? latestContract.getStatus() : null)
                    .terms(latestContract != null ? latestContract.getTerms() : null)
                    .createdAt(latestContract != null ? latestContract.getCreatedAt() : null)
                    .updatedAt(latestContract != null ? latestContract.getUpdatedAt() : null)

                    .bookingRequestId(booking.getId())
                    .bookingDescription(booking.getDescription())
                    .bookingStatus(booking.getStatus())

                    .paymentSchedules(payments)
                    .build();
        });

        return ApiResponse.<PagedResponse<UserContractResponse>>builder()
                .status(200)
                .message(List.of("Lấy danh sách hợp đồng thành công"))
                .data(PagedResponse.fromPage(mapped))
                .build();
    }

}

