package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.temp_models.Deliverable;
import com.web.bookingKol.temp_models.Offer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "platforms")
public class Platform {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @Column(name = "key", length = 50)
    private String key;

    @Size(max = 120)
    @Column(name = "name", length = 120)
    private String name;

    @OneToMany(mappedBy = "platform")
    private Set<Deliverable> deliverables = new LinkedHashSet<>();

    @OneToMany(mappedBy = "platform")
    private Set<KolSocialAccount> kolSocialAccounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "platform")
    private Set<Offer> offers = new LinkedHashSet<>();

}