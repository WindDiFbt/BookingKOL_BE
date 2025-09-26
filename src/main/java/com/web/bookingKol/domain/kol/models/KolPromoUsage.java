package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.temp_models.Contract;
import com.web.bookingKol.temp_models.Offer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kol_promo_usages")
public class KolPromoUsage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_promo_id", nullable = false)
    private KolPromotion kolPromo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "applied_pct", precision = 6, scale = 3)
    private BigDecimal appliedPct;

    @Column(name = "applied_amt", precision = 18, scale = 2)
    private BigDecimal appliedAmt;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}