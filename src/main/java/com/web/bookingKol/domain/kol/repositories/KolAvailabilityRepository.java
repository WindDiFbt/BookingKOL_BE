package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.user.models.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface KolAvailabilityRepository extends JpaRepository<KolAvailability, UUID> {
    List<KolAvailability> findByUser(User user);
    List<KolAvailability> findByUserAndStartAtBetween(User user, OffsetDateTime start, OffsetDateTime end);

    @Query(value = """
    SELECT * FROM kol_availabilities WHERE user_id = :userId
      AND (COALESCE(:start, start_at) <= start_at)
      AND (COALESCE(:end, end_at) >= end_at)
    ORDER BY start_at
""", nativeQuery = true)
    List<KolAvailability> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
        SELECT k FROM KolAvailability k
        WHERE k.user.id = :userId
          AND (:start IS NULL OR k.startAt >= :start)
          AND (:end IS NULL OR k.endAt <= :end)
          AND (:status IS NULL OR k.status = :status)
          AND (:note IS NULL OR LOWER(k.note) LIKE LOWER(CONCAT('%', :note, '%')))
        ORDER BY k.startAt
    """)
    List<KolAvailability> search(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("status") String status,
            @Param("note") String note
    );




}

