package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent paymentIntent;

    @Column(name = "captured_amount", precision = 18, scale = 2)
    private BigDecimal capturedAmount;

    @Size(max = 255)
    @Column(name = "provider_charge_id")
    private String providerChargeId;

    @Column(name = "succeeded_at")
    private Instant succeededAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", length = Integer.MAX_VALUE)
    private String failureReason;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "payment")
    private Set<Refund> refunds = new LinkedHashSet<>();

    @Size(max = 255)
    @Column(name = "transaction_id")
    private String transactionId;

    @OneToMany(mappedBy = "payment")
    private Set<PurchasedCoursePackage> purchasedCoursePackages = new LinkedHashSet<>();

}