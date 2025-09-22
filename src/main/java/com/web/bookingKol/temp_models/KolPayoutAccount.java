package com.web.bookingKol.temp_models;

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
@Table(name = "kol_payout_accounts")
public class KolPayoutAccount {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @Size(max = 50)
    @Column(name = "provider", length = 50)
    private String provider;

    @Size(max = 255)
    @Column(name = "account_name")
    private String accountName;

    @Size(max = 255)
    @Column(name = "account_number")
    private String accountNumber;

    @Size(max = 50)
    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Size(max = 255)
    @Column(name = "external_ref")
    private String externalRef;

    @ColumnDefault("true")
    @Column(name = "is_default")
    private Boolean isDefault;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "payoutAccount")
    private Set<Payout> payouts = new LinkedHashSet<>();

}