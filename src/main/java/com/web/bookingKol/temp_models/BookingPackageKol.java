package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.kol.models.KolProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "booking_package_kols")
public class BookingPackageKol {
    @EmbeddedId
    private BookingPackageKolId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("purchasedPackageId")
    @JoinColumn(name = "purchased_package_id", nullable = false)
    private PurchasedServicePackage purchasedPackage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("kolId")
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}

@Embeddable
class BookingPackageKolId {
    @Column(name = "purchased_package_id")
    private String purchasedPackageId;

    @Column(name = "kol_id")
    private String kolId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingPackageKolId that)) return false;
        return purchasedPackageId.equals(that.purchasedPackageId) && kolId.equals(that.kolId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(purchasedPackageId, kolId);
    }
}