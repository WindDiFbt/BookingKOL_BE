package com.web.bookingKol.temp_models;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = Integer.MAX_VALUE)
    private String reason;

    @Size(max = 255)
    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}