package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolPromoUsage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @Size(max = 100)
    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "terms", length = Integer.MAX_VALUE)
    private String terms;

    @Column(name = "signed_at_brand")
    private Instant signedAtBrand;

    @Column(name = "signed_at_kol")
    private Instant signedAtKol;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "contract")
    private Set<ContractPromo> contractPromos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<ContractTax> contractTaxes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<Deliverable> deliverables = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<Dispute> disputes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<KolFeedback> kolFeedbacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<KolPromoUsage> kolPromoUsages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<PaymentIntent> paymentIntents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<Payout> payouts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<Review> reviews = new LinkedHashSet<>();

}