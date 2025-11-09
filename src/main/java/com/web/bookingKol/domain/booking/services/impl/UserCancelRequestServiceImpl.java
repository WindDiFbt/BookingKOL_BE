package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.*;
import com.web.bookingKol.domain.booking.repositories.*;
import com.web.bookingKol.domain.booking.services.UserBookingViewService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCancelRequestServiceImpl implements UserBookingViewService  {

    private final BookingRequestRepository bookingRequestRepository;
    private final ContractRepository contractRepository;
    private final ContractPaymentScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BookingCancellationPenaltyRepository penaltyRepository;

    @Override
    @Transactional
    public ApiResponse<?> cancelBookingRequest(UUID bookingRequestId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        BookingRequest booking = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking request"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy booking request này");
        }

        // Lấy hợp đồng mới nhất
        Contract contract = booking.getContracts().stream()
                .max(Comparator.comparing(Contract::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        List<ContractPaymentSchedule> schedules =
                scheduleRepository.findByContract_Id(contract.getId());

        if (schedules.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông tin thanh toán");
        }

        LocalDate today = LocalDate.now();
        LocalDate bookingStart = booking.getStartAt() != null
                ? booking.getStartAt().atZone(ZoneId.systemDefault()).toLocalDate()
                : today;
        ContractPaymentSchedule firstSchedule = schedules.stream()
                .min(Comparator.comparing(ContractPaymentSchedule::getDueDate))
                .orElseThrow();

        BigDecimal firstAmount = firstSchedule.getAmount();
        BigDecimal penaltyPercent = BigDecimal.ZERO;
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        String reason = "";

        boolean allPending = schedules.stream()
                .allMatch(s -> s.getStatus() == Enums.PaymentScheduleStatus.PENDING);

        boolean anyPaid = schedules.stream()
                .anyMatch(s -> s.getStatus() == Enums.PaymentScheduleStatus.PAID);

        if (allPending) {
            booking.setStatus(Enums.BookingStatus.CANCELLED.name());
            bookingRequestRepository.save(booking);

            return ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Hủy booking thành công, không bị phạt"))
                    .data(new Object() {
                        public final UUID bookingRequestId = booking.getId();
                        public final String status = booking.getStatus();
                        public final String penalty = "0%";
                    })
                    .build();
        }

        if (anyPaid) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, bookingStart);

            if (daysBetween <= 3) {
                penaltyPercent = BigDecimal.ONE;
                reason = "Hủy sát ngày, bị phạt 100% tiền đợt 1.";
            } else {
                penaltyPercent = new BigDecimal("0.5");
                reason = "Hủy sớm, bị phạt 50% tiền đợt 1.";
            }

            penaltyAmount = firstAmount.multiply(penaltyPercent);

            BookingCancellationPenalty penalty = new BookingCancellationPenalty();
            penalty.setBookingRequest(booking);
            penalty.setPenaltyPercent(penaltyPercent);
            penalty.setPenaltyAmount(penaltyAmount);
            penalty.setReason(reason);
            penaltyRepository.save(penalty);

            booking.setStatus(Enums.BookingStatus.CANCELLED.name());
            bookingRequestRepository.save(booking);

            return ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Hủy booking thành công, có áp dụng tiền phạt"))
                    .data(new Object() {
                        public final UUID bookingRequestId = booking.getId();
                        public final String status = booking.getStatus();
                    })
                    .build();
        }

        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(List.of("Không thể hủy booking request trong trạng thái hiện tại"))
                .build();
    }
}

