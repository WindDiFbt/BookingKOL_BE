package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolPromoUsage;
import com.web.bookingKol.domain.kol.models.KolPromotion;
import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "brands")
public class Brand {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Size(max = 255)
    @Column(name = "company_legal_name")
    private String companyLegalName;

    @Size(max = 50)
    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "website_url", length = Integer.MAX_VALUE)
    private String websiteUrl;

    @Column(name = "logo_url", length = Integer.MAX_VALUE)
    private String logoUrl;

    @Column(name = "billing_address", length = Integer.MAX_VALUE)
    private String billingAddress;

    @Size(max = 255)
    @Column(name = "contact_person")
    private String contactPerson;

    @Size(max = 255)
    @Column(name = "contact_email")
    private String contactEmail;

    @Size(max = 50)
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

}