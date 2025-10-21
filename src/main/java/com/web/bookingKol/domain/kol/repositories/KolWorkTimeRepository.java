package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface KolWorkTimeRepository extends JpaRepository<KolWorkTime, UUID> {

    @Query("""
                SELECT CASE WHEN COUNT(kt) > 0 THEN TRUE ELSE FALSE END
                FROM KolWorkTime kt
                INNER JOIN KolAvailability ka ON ka.id = kt.availability.id
                WHERE ka.kol.id = :kolId
                  AND kt.status NOT IN ('CANCELLED')
                  AND kt.startAt = :startAt
                  AND kt.endAt = :endAt
            """)
    boolean existsRequestSameTime(@Param("kolId") UUID kolId,
                                  @Param("startAt") Instant startAt,
                                  @Param("endAt") Instant endAt);

    @Query("""
                SELECT COUNT(kt) > 0
                FROM KolWorkTime kt
                INNER JOIN KolAvailability ka ON ka.id = kt.availability.id
                WHERE ka.kol.id = :kolId
                  AND kt.status NOT IN ('CANCELLED')
                  AND kt.startAt < :newEndAt
                  AND kt.endAt > :newStartAt
            """)
    boolean existsOverlappingBooking(
            @Param("kolId") UUID kolId,
            @Param("newStartAt") Instant newStartAt,
            @Param("newEndAt") Instant newEndAt
    );

    @Query("""
                SELECT kt FROM KolWorkTime kt
                INNER JOIN KolAvailability ka ON ka.id = kt.availability.id
                WHERE ka.kol.id = :kolId
                  AND kt.status NOT IN ('CANCELLED')
                  AND kt.endAt <= :startAt
                ORDER BY kt.endAt DESC
            """)
    List<KolWorkTime> findFirstPreviousBooking(
            @Param("kolId") UUID kolId,
            @Param("startAt") Instant startAt
    );

    @Query("""
        SELECT wt FROM KolWorkTime wt
        WHERE wt.availability.kol.id = :kolId
          AND wt.status = 'BOOKED'
          AND (CAST(:startDate AS timestamp) IS NULL OR wt.endAt >= :startDate)
          AND (CAST(:endDate AS timestamp) IS NULL OR wt.startAt <= :endDate)
        ORDER BY wt.startAt ASC
    """)
    List<KolWorkTime> findBookedTimes(
            @Param("kolId") UUID kolId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
                SELECT kt FROM KolWorkTime kt
                INNER JOIN KolAvailability ka ON ka.id = kt.availability.id
                WHERE ka.kol.id = :kolId
                  AND kt.status NOT IN ('CANCELLED')
                  AND kt.startAt >= :endAt
                ORDER BY kt.startAt ASC
            """)
    List<KolWorkTime> findNextBooking(
            @Param("kolId") UUID kolId,
            @Param("endAt") Instant endAt
    );




    List<KolWorkTime> findByAvailability_Id(UUID availabilityId);


}
