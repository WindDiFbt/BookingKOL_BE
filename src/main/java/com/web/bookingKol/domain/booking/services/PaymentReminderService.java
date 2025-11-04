package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    private final ContractPaymentScheduleRepository scheduleRepository;
    private final EmailService emailService;


    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendPaymentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(3);

        List<ContractPaymentSchedule> upcomingPayments =
                scheduleRepository.findByStatusAndDueDateBetween(
                        Enums.PaymentScheduleStatus.PENDING,
                        today,
                        reminderDate
                );

        log.info("Tìm thấy {} lịch thanh toán sắp đến hạn.", upcomingPayments.size());

        for (ContractPaymentSchedule schedule : upcomingPayments) {
            Contract contract = schedule.getContract();
            BookingRequest booking = contract.getBookingRequest();
            String email = booking.getUser().getEmail();
            String userName = booking.getUser().getFullName();
            BigDecimal amount = schedule.getAmount();
            LocalDate dueDate = schedule.getDueDate();

            try {
                emailService.sendPaymentReminderEmail(
                        email,
                        userName,
                        contract.getContractNumber(),
                        schedule.getInstallmentNumber(),
                        amount,
                        dueDate
                );
                log.info("Đã gửi email nhắc thanh toán cho {}", email);
            } catch (Exception e) {
                log.error("Lỗi khi gửi mail cho {}: {}", email, e.getMessage());
            }
        }
    }
}

