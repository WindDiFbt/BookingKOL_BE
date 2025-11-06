package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.booking.models.BookingPackageKol;
import com.web.bookingKol.domain.booking.models.Campaign;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchased_service_package")
public class PurchasedServicePackage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage packageField;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "price")
    private Integer price;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Size(max = 50)
    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "purchasedPackage")
    private Set<BookingPackageKol> bookingPackageKols = new LinkedHashSet<>();

    @Column(name = "booking_number", length = 50, unique = true)
    private String bookingNumber;

    @PrePersist
    public void prePersist() {
        if (this.bookingNumber == null || this.bookingNumber.isBlank()) {
            this.bookingNumber = "BK-" + System.currentTimeMillis();
        }
    }


}