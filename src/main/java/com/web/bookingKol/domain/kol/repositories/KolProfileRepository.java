package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.kol.models.KolProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KolProfileRepository extends JpaRepository<KolProfile, UUID> {
    @Query(""" 
            SELECT k FROM KolProfile k 
            LEFT JOIN FETCH k.fileUsages fu
            LEFT JOIN FETCH fu.file f
            WHERE f.fileType IN ('IMAGE', 'VIDEO') AND f.status = 'ACTIVE'
            AND k.user.id = :userId AND fu.isActive = true ORDER BY f.fileType DESC , fu.createdAt DESC
            """)
    Optional<KolProfile> findByUserId(@Param("userId") UUID userId);

    @Query(""" 
            SELECT k FROM KolProfile k 
            LEFT JOIN FETCH k.fileUsages fu
            LEFT JOIN FETCH fu.file f
            WHERE f.fileType IN ('IMAGE', 'VIDEO') AND f.status = 'ACTIVE'
            AND k.id = :kolId AND fu.isActive = true ORDER BY f.fileType DESC , fu.createdAt DESC
            """)
    Optional<KolProfile> findByKolId(@Param("kolId") UUID kolId);

    @Query("SELECT k FROM KolProfile k JOIN k.categories c WHERE c.id = :CategoryId")
    List<KolProfile> findByCategoryId(UUID CategoryId);

    @Query("""
                SELECT k FROM KolProfile k
                LEFT JOIN FETCH k.user u
                LEFT JOIN k.categories c
                WHERE k.isAvailable = true
                  AND u.status = :userStatus
                  AND (:minRating IS NULL OR k.overallRating >= :minRating)
                  AND (:categoryId IS NULL OR c.id = :categoryId)
                  AND (:minPrice IS NULL OR k.minBookingPrice >= :minPrice)
                  AND (:nameKeyword IS NULL OR LOWER(k.displayName) LIKE %:nameKeyword%)
                  AND (:role IS NULL OR k.role = :role)
            """)
    Page<KolProfile> findAllKolAvailableWithFilter(
            @Param("userStatus") String userStatus,
            @Param("minRating") Double minRating,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("role") Enums.Roles role,
            @Param("nameKeyword") String nameKeyword,
            Pageable pageable
    );

    boolean existsById(UUID id);

    @Query("""
                SELECT k FROM KolProfile k
                WHERE (:minBookingPrice IS NULL OR k.minBookingPrice >= :minBookingPrice)
                  AND (:isAvailable IS NULL OR k.isAvailable = :isAvailable)
                  AND (:minRating IS NULL OR k.overallRating >= :minRating)
                  AND (:role IS NULL OR k.role = :role)
                  AND (:nameKeyword IS NULL OR LOWER(k.displayName) LIKE %:nameKeyword%)
            """)
    Page<KolProfile> findAllFiltered(
            @Param("minBookingPrice") BigDecimal minBookingPrice,
            @Param("isAvailable") Boolean isAvailable,
            @Param("minRating") Double minRating,
            @Param("role") Enums.Roles role,
            @Param("nameKeyword") String nameKeyword,
            Pageable pageable
    );

}
