package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.temp_models.BookingRequest;
import com.web.bookingKol.temp_models.Campaign;
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
@Table(name = "kol_promotions")
public class KolPromotion {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

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

    @Size(max = 50)
    @Column(name = "scope", length = 50)
    private String scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ColumnDefault("true")
    @Column(name = "is_public")
    private Boolean isPublic;

    @ColumnDefault("false")
    @Column(name = "stackable")
    private Boolean stackable;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "kolPromo")
    private Set<BookingRequest> bookingRequests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kolPromo")
    private Set<KolPromoUsage> kolPromoUsages = new LinkedHashSet<>();

}