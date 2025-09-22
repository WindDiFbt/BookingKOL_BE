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
@Table(name = "promo_codes")
public class PromoCode {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "discount_pct", precision = 6, scale = 3)
    private BigDecimal discountPct;

    @Column(name = "discount_amt", precision = 18, scale = 2)
    private BigDecimal discountAmt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @ColumnDefault("0")
    @Column(name = "used_count")
    private Integer usedCount;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "promoCode")
    private Set<Campaign> campaigns = new LinkedHashSet<>();

    @OneToMany(mappedBy = "promoCode")
    private Set<ContractPromo> contractPromos = new LinkedHashSet<>();

}