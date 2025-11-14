package com.web.bookingKol.domain.payment.models;

import com.web.bookingKol.domain.booking.models.Contract;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refunds")
public class Refund {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = Integer.MAX_VALUE)
    private String reason;

    @Size(max = 20)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Size(max = 20)
    @Column(name = "bank_number", length = 20)
    private String bankNumber;

    @Size(max = 255)
    @Column(name = "bank_name")
    private String bankName;

    @Size(max = 255)
    @Column(name = "owner_name")
    private String ownerName;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

}