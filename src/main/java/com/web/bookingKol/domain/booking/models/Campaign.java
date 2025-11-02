package com.web.bookingKol.domain.booking.models;

import com.web.bookingKol.domain.kol.models.KolPromotion;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.temp_models.Conversation;
import com.web.bookingKol.temp_models.PromoCode;
import com.web.bookingKol.temp_models.PurchasedServicePackage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "objective", length = Integer.MAX_VALUE)
    private String objective;

    @Column(name = "budget_min", precision = 18, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 18, scale = 2)
    private BigDecimal budgetMax;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "campaign")
    private Set<BookingRequest> bookingRequests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "campaign")
    private Set<Brief> briefs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "campaign")
    private Set<Conversation> conversations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "campaign")
    private Set<KolPromotion> kolPromotions = new LinkedHashSet<>();


    @OneToMany(mappedBy = "campaign")
    private Set<PurchasedServicePackage> purchasedServicePackages = new LinkedHashSet<>();

}