package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.temp_models.BookingRequest;
import com.web.bookingKol.temp_models.Payout;
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
@Table(name = "kol_profiles")
public class KolProfile {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "bio", length = Integer.MAX_VALUE)
    private String bio;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 200)
    @Column(name = "languages", length = 200)
    private String languages;

    @Column(name = "rate_card_note", length = Integer.MAX_VALUE)
    private String rateCardNote;

    @Column(name = "min_booking_price", precision = 18, scale = 2)
    private BigDecimal minBookingPrice;

    @ColumnDefault("true")
    @Column(name = "is_available")
    private Boolean isAvailable;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "kol")
    private Set<BookingRequest> bookingRequests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolAvailability> kolAvailabilities = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
        name = "kol_categories",
        joinColumns = @JoinColumn(name = "kol_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolFeedback> kolFeedbacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolHourlyRate> kolHourlyRates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolPayoutAccount> kolPayoutAccounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolPromoUsage> kolPromoUsages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolPromotion> kolPromotions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolRatePackage> kolRatePackages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<KolSocialAccount> kolSocialAccounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "kol")
    private Set<Payout> payouts = new LinkedHashSet<>();

    @Column(name = "overall_rating")
    private Double overallRating;

    @Column(name = "feedback_count")
    private Integer feedbackCount;

}