package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContractPaymentScheduleRepository extends JpaRepository<ContractPaymentSchedule, UUID> {

    List<ContractPaymentSchedule> findByContract_Id(UUID contractId);

    List<ContractPaymentSchedule> findByBookingRequest_Id(UUID bookingRequestId);

    List<ContractPaymentSchedule> findByStatusAndDueDateBetween(
            Enums.PaymentScheduleStatus status,
            LocalDate from,
            LocalDate to
    );

}

