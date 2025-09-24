package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "service_packages")
public class ServicePackage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 20)
    @Column(name = "package_type", length = 20)
    private String packageType;

    @ColumnDefault("false")
    @Column(name = "allow_kol_selection")
    private Boolean allowKolSelection;

    @OneToMany(mappedBy = "packageField")
    private Set<PurchasedServicePackage> purchasedServicePackages = new LinkedHashSet<>();

}