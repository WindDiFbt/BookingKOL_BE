package com.web.bookingKol.temp_models;

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
@Table(name = "commissions")
public class Commission {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 120)
    @Column(name = "name", length = 120)
    private String name;

    @NotNull
    @Column(name = "rate_percent", nullable = false, precision = 6, scale = 3)
    private BigDecimal ratePercent;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "commission")
    private Set<PaymentIntent> paymentIntents = new LinkedHashSet<>();

}