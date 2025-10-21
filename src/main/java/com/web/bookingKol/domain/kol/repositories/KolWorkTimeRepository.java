package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolWorkTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
}
