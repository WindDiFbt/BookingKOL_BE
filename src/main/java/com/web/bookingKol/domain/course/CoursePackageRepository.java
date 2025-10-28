package com.web.bookingKol.domain.course;

import com.web.bookingKol.domain.course.models.CoursePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoursePackageRepository extends JpaRepository<CoursePackage, UUID> {
    @Query("""
                SELECT DISTINCT cp
                FROM CoursePackage cp
                LEFT JOIN FETCH cp.fileUsages fu
                LEFT JOIN FETCH fu.file f
                WHERE cp.isAvailable = true AND cp.id = :id AND fu.targetType = :targetType
            """)
    Optional<CoursePackage> findByCoursePackageId(@Param("id") UUID id, @Param("targetType") String targetType);

    @Query("""
                SELECT c FROM CoursePackage c
                WHERE c.isAvailable = true
                  AND (:minPrice IS NULL OR c.price >= :minPrice)
                  AND (:maxPrice IS NULL OR c.price <= :maxPrice)
                  AND (:minDiscount IS NULL OR c.discount >= :minDiscount)
                  AND (:maxDiscount IS NULL OR c.discount <= :maxDiscount)
            """)
    Page<CoursePackage> findAllAvailableForUser(
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("maxDiscount") Integer maxDiscount,
            Pageable pageable
    );

    @Query("""
                SELECT c FROM CoursePackage c
                WHERE (:isAvailable IS NULL OR c.isAvailable = :isAvailable)
                  AND (:minPrice IS NULL OR c.price >= :minPrice)
                  AND (:maxPrice IS NULL OR c.price <= :maxPrice)
                  AND (:minDiscount IS NULL OR c.discount >= :minDiscount)
                  AND (:maxDiscount IS NULL OR c.discount <= :maxDiscount)
            """)
    Page<CoursePackage> findAllFiltered(
            @Param("isAvailable") Boolean isAvailable,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("maxDiscount") Integer maxDiscount,
            Pageable pageable
    );
}
