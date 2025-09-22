package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
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
@Table(name = "taxes")
public class Tax {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 120)
    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "rate_percent", precision = 6, scale = 3)
    private BigDecimal ratePercent;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "tax")
    private Set<ContractTax> contractTaxes = new LinkedHashSet<>();

}