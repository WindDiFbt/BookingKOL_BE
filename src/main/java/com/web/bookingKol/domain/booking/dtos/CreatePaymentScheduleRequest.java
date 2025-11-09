package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePaymentScheduleRequest {

    @NotNull(message = "ID hợp đồng không được để trống")
    private UUID contractId;

    @NotNull(message = "ID booking request không được để trống")
    private UUID bookingRequestId;

    @Min(value = 1, message = "Số lần thanh toán phải ít nhất là 1")
    private Integer totalInstallments;

    @NotNull(message = "Danh sách đợt thanh toán không được để trống")
    private List<InstallmentItem> installments;

    @Data
    public static class InstallmentItem {
        private BigDecimal amount;
        private LocalDate dueDate;
    }
}

