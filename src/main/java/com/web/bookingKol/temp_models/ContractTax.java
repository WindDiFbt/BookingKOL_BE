package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contract_taxes")
public class ContractTax {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tax_id", nullable = false)
    private Tax tax;

    @Column(name = "applied_pct", precision = 6, scale = 3)
    private BigDecimal appliedPct;

    @Column(name = "applied_amt", precision = 18, scale = 2)
    private BigDecimal appliedAmt;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}