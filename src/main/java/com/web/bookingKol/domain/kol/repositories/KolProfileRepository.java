package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KolProfileRepository extends JpaRepository<KolProfile, UUID> {
    @Query(""" 
            SELECT k FROM KolProfile k 
            LEFT JOIN FETCH k.fileUsages fu
            LEFT JOIN FETCH fu.file f
            WHERE k.user.id = :userId AND fu.targetType = :targetType
            """)
    Optional<KolProfile> findByUserId(@Param("userId") UUID userId, @Param("targetType") String targetType);

    @Query(""" 
            SELECT k FROM KolProfile k 
            LEFT JOIN FETCH k.fileUsages fu
            LEFT JOIN FETCH fu.file f
            WHERE k.id = :kolId AND fu.targetType = :targetType
            """)
    Optional<KolProfile> findByKolId(@Param("kolId") UUID kolId, @Param("targetType") String targetType);

    @Query("SELECT k FROM KolProfile k JOIN k.categories c WHERE c.id = :CategoryId")
    List<KolProfile> findByCategoryId(UUID CategoryId);

    @Query("SELECT k FROM KolProfile k WHERE k.isAvailable = true")
    List<KolProfile> findAllKolAvailableProfiles();

    @Query("""
                SELECT k FROM KolProfile k
                JOIN k.categories c
                WHERE (:minRating IS NULL OR k.overallRating >= :minRating)
                  AND (:categoryId IS NULL OR c.id = :categoryId)
                  AND (:minPrice IS NULL OR k.minBookingPrice >= :minPrice)
                  AND (:city IS NULL OR :city = '' OR k.city = :city)
            """)
    List<KolProfile> filterKols(
            @Param("minRating") Double minRating,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") Double minPrice,
            @Param("city") String city
    );
}
