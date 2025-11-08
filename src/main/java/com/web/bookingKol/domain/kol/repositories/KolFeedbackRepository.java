package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface KolFeedbackRepository extends JpaRepository<KolFeedback, UUID> {
    @Query("SELECT k FROM KolFeedback k WHERE k.kol.id = :kolId")
    Page<KolFeedback> getAllByKolId(UUID kolId, Pageable pageable);

    @Query("SELECT k FROM KolFeedback k WHERE k.reviewerUser.id = :userId")
    Page<KolFeedback> getAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT f FROM KolFeedback f JOIN FETCH f.reviewerUser " +
            "WHERE f.kol.id = :kolId " +
            "AND f.createdAt BETWEEN :startDate AND :endDate")
    List<KolFeedback> findByKolIdAndDateRangeWithReviewer(
            @Param("kolId") UUID kolId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
