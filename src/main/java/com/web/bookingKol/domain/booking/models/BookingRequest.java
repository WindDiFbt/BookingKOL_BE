package com.web.bookingKol.domain.booking.models;

import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolPromotion;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.user.models.User;
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
@Table(name = "booking_requests")
public class BookingRequest {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "campaign_id", nullable = true)
    private Campaign campaign;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = true)
    private KolProfile kol;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

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

    @Column(name = "contract_amount", precision = 15, scale = 2)
    private BigDecimal contractAmount;

    @OneToMany(mappedBy = "bookingRequest")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "bookingRequest")
    private Set<Offer> offers = new LinkedHashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "booking_type", length = 50)
    private String bookingType;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @OneToMany
    @JoinColumn(name = "target_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<FileUsage> attachedFiles = new LinkedHashSet<>();

    @Column(name = "location", length = Integer.MAX_VALUE)
    private String location;

    @OneToMany(mappedBy = "bookingRequest")
    private Set<KolWorkTime> kolWorkTimes = new LinkedHashSet<>();

    @Size(max = 255)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Size(max = 50)
    @Column(name = "phone", length = 50)
    private String phone;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 20)
    @Column(name = "request_number", unique = true, nullable = false)
    private String requestNumber;

    @Size(max = 20)
    @Column(name = "platform", length = 20)
    private String platform;

}