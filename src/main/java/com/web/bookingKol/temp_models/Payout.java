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
@Table(name = "payouts")
public class Payout {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payout_account_id", nullable = false)
    private KolPayoutAccount payoutAccount;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Size(max = 10)
    @ColumnDefault("'VND'")
    @Column(name = "currency", length = 10)
    private String currency;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Size(max = 255)
    @Column(name = "provider_payout_id")
    private String providerPayoutId;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "failure_reason", length = Integer.MAX_VALUE)
    private String failureReason;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}