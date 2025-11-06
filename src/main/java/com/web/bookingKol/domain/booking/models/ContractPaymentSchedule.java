package com.web.bookingKol.domain.booking.models;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.payment.models.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contract_payment_schedules")
public class ContractPaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    // Liên kết với hợp đồng
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // Liên kết với booking request
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    // Thứ tự đợt thanh toán
    @Column(name = "installment_number")
    private Integer installmentNumber;

    // Số tiền mỗi đợt
    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    // Ngày thanh toán dự kiến
    @Column(name = "due_date")
    private LocalDate dueDate;

    // Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private Enums.PaymentScheduleStatus status;


    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "paymentSchedule")
    private Transaction transaction;

}

