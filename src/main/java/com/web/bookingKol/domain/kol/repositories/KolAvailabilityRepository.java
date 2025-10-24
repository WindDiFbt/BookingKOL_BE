package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
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
            @Param("start") Instant start,
            @Param("end") Instant end
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
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
                SELECT ka FROM KolAvailability ka
                WHERE ka.kol.id = :kolId
                  AND (COALESCE(:startDate, ka.startAt) <= ka.startAt)
                  AND (COALESCE(:endDate, ka.endAt) >= ka.endAt)
            """)
    Page<KolAvailability> findByKolIdAndDateRangePaged(
            @Param("kolId") UUID kolId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT ka FROM KolAvailability ka WHERE ka.kol.id = :kolId AND ka.startAt <= :startTime AND ka.endAt >= :endTime")
    KolAvailability findAvailability(@Param("kolId") UUID kolId,
                                     @Param("startTime") Instant startTime,
                                     @Param("endTime") Instant endTime);


    @Query("""
                SELECT ka FROM KolAvailability ka
                WHERE ka.kol.id = :kolId
                  AND ka.status = 'AVAILABLE'
                  AND (CAST(:startDate AS timestamp) IS NULL OR ka.endAt >= :startDate)
                  AND (CAST(:endDate AS timestamp) IS NULL OR ka.startAt <= :endDate)
                ORDER BY ka.startAt ASC
            """)
    List<KolAvailability> findAvailabilities(
            @Param("kolId") UUID kolId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

}

