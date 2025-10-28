package com.web.bookingKol.domain.course.models;

import com.web.bookingKol.domain.file.models.FileUsage;
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
@Table(name = "course_packages")
public class CoursePackage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "current_price")
    private Long currentPrice;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @OneToMany(mappedBy = "coursePackage")
    private Set<PurchasedCoursePackage> purchasedCoursePackages = new LinkedHashSet<>();

    @OneToMany
    @JoinColumn(name = "target_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<FileUsage> fileUsages = new LinkedHashSet<>();

    @Column(name = "is_available")
    private Boolean isAvailable;

}