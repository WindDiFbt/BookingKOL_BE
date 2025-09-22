package com.web.bookingKol.domain.kol.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kol_hourly_rates")
public class KolHourlyRate {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @Size(max = 10)
    @ColumnDefault("'VND'")
    @Column(name = "currency", length = 10)
    private String currency;

    @NotNull
    @Column(name = "base_hourly_rate", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseHourlyRate;

    @ColumnDefault("1")
    @Column(name = "min_hours")
    private Integer minHours;

    @Column(name = "overtime_hourly_rate", precision = 18, scale = 2)
    private BigDecimal overtimeHourlyRate;

    @Column(name = "weekend_hourly_rate", precision = 18, scale = 2)
    private BigDecimal weekendHourlyRate;

    @Column(name = "note", length = Integer.MAX_VALUE)
    private String note;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}