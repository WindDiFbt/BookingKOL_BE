package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.user.models.User;
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
@Table(name = "offers")
public class Offer {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sent_by", nullable = false)
    private User sentBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Size(max = 50)
    @Column(name = "offer_type", length = 50)
    private String offerType;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

    @Size(max = 10)
    @ColumnDefault("'VND'")
    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    @Column(name = "proposed_start_at")
    private Instant proposedStartAt;

    @Column(name = "proposed_end_at")
    private Instant proposedEndAt;

    @Column(name = "valid_until")
    private Instant validUntil;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "offer")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "offer")
    private Set<KolPromoUsage> kolPromoUsages = new LinkedHashSet<>();

}