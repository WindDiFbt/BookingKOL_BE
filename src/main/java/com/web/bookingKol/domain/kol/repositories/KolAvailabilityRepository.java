package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface KolAvailabilityRepository extends JpaRepository<KolAvailability, UUID> {
//    List<KolAvailability> findByUser(User user);
//
//    List<KolAvailability> findByUserAndStartAtBetween(User user, OffsetDateTime start, OffsetDateTime end);

    @Query(value = """
                SELECT * FROM kol_availabilities WHERE kol_id = :kolId
                  AND (COALESCE(:start, start_at) <= start_at)
                  AND (COALESCE(:end, end_at) >= end_at)
                ORDER BY start_at
            """, nativeQuery = true)
    List<KolAvailability> findByKolIdAndDateRange(
            @Param("kolId") UUID kolId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
                SELECT CASE WHEN COUNT(ka) > 0 THEN TRUE ELSE FALSE END
                FROM KolAvailability ka
                WHERE ka.kol.id = :kolId
                  AND ka.status = 'AVAILABLE'
                  AND :start >= ka.startAt
                  AND :end <= ka.endAt
            """)
    boolean isKolAvailabilityInRange(
            @Param("kolId") UUID kolId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
                SELECT a FROM KolAvailability a
                WHERE a.kol.id = :kolId
                AND (:startDate IS NULL OR a.startAt >= :startDate)
                AND (:endDate IS NULL OR a.endAt <= :endDate)
                ORDER BY a.startAt DESC
            """)
    Page<KolAvailability> findByKolIdAndDateRangePaged(
            @Param("kolId") UUID kolId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT ka FROM KolAvailability ka WHERE ka.kol.id = :kolId AND ka.startAt <= :startTime AND ka.endAt >= :endTime")
    KolAvailability findAvailability(@Param("kolId") UUID kolId,
                                     @Param("startTime") OffsetDateTime startTime,
                                     @Param("endTime") OffsetDateTime endTime);

}

