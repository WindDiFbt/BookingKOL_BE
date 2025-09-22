package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolPromotion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "booking_requests")
public class BookingRequest {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "attached_file", length = Integer.MAX_VALUE)
    private String attachedFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kol_promo_id")
    private KolPromotion kolPromo;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Size(max = 20)
    @Column(name = "repeat_type", length = 20)
    private String repeatType;

    @Size(max = 20)
    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;

    @Column(name = "repeat_until")
    private LocalDate repeatUntil;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "bookingRequest")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "bookingRequest")
    private Set<Offer> offers = new LinkedHashSet<>();

}