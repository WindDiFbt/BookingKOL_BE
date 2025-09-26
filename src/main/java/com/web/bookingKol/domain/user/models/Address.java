package com.web.bookingKol.domain.user.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 120)
    @Column(name = "label", length = 120)
    private String label;

    @Size(max = 255)
    @Column(name = "line1")
    private String line1;

    @Size(max = 255)
    @Column(name = "line2")
    private String line2;

    @Size(max = 120)
    @Column(name = "city", length = 120)
    private String city;

    @Size(max = 120)
    @Column(name = "state", length = 120)
    private String state;

    @Size(max = 30)
    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Size(max = 120)
    @Column(name = "country", length = 120)
    private String country;

    @ColumnDefault("false")
    @Column(name = "is_default")
    private Boolean isDefault;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}