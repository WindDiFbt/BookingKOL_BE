package com.web.bookingKol.domain.course;

import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.temp_models.PurchasedCoursePackage;
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
    private Integer price;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @OneToMany(mappedBy = "coursePackage")
    private Set<PurchasedCoursePackage> purchasedCoursePackages = new LinkedHashSet<>();

//    @OneToMany(mappedBy = "packageField", cascade =  CascadeType.ALL)
//    private Set<CoursePackageMedia> coursePackageMedia = new LinkedHashSet<>();

    @OneToMany
    @JoinColumn(name = "target_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<FileUsage> fileUsages;
}