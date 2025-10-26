package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.booking.models.LivestreamMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LivestreamMetricRepository extends JpaRepository<LivestreamMetric, Integer> {

    @Query("SELECT l FROM LivestreamMetric l WHERE l.kolWorkTime.availability.kol.id = :kolId AND l.isConfirmed = true")
    List<LivestreamMetric> findAllByKolId(UUID kolId);

    @Query("SELECT l FROM LivestreamMetric l WHERE l.kolWorkTime.id = :workTimeId")
    LivestreamMetric findByWorkTimeId(@Param("workTimeId") UUID workTimeId);
}
