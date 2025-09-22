package com.web.bookingKol.domain.kol.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 120)
    @NotNull
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 120)
    @NotNull
    @Column(name = "key", nullable = false, length = 120)
    private String key;

    @ManyToMany
    private Set<KolProfile> kolProfiles = new LinkedHashSet<>();

}