package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, UUID>, JpaSpecificationExecutor<BookingRequest> {
    @Query("""
                SELECT br FROM BookingRequest br
                WHERE br.kol.id = :kolId
                  AND br.status NOT IN ('REJECTED', 'CANCELLED', 'DISPUTED', 'EXPIRED')
                  AND br.endAt <= :startAt
                ORDER BY br.endAt DESC
            """)
    List<BookingRequest> findFirstPreviousBooking(
            @Param("kolId") UUID kolId,
            @Param("startAt") Instant startAt
    );

    @Query("""
                SELECT br FROM BookingRequest br
                WHERE br.kol.id = :kolId
                  AND br.status NOT IN ('REJECTED', 'CANCELLED', 'DISPUTED', 'EXPIRED')
                  AND br.startAt >= :endAt
                ORDER BY br.startAt ASC
            """)
    List<BookingRequest> findNextBooking(
            @Param("kolId") UUID kolId,
            @Param("endAt") Instant endAt
    );

    @Query("""
                SELECT CASE WHEN COUNT(br) > 0 THEN TRUE ELSE FALSE END
                FROM BookingRequest br
                WHERE br.kol.id = :kolId
                  AND br.status NOT IN ('REJECTED', 'CANCELLED', 'DISPUTED', 'EXPIRED')
                  AND br.startAt = :startAt
                  AND br.endAt = :endAt
            """)
    boolean existsRequestSameTime(@Param("kolId") UUID kolId,
                                  @Param("startAt") Instant startAt,
                                  @Param("endAt") Instant endAt);


    @Query("""
                SELECT COUNT(b) > 0
                FROM BookingRequest b
                WHERE b.kol.id = :kolId
                  AND b.status NOT IN ('REJECTED', 'CANCELLED','DISPUTED', 'EXPIRED')
                  AND b.startAt < :newEndAt
                  AND b.endAt > :newStartAt
            """)
    boolean existsOverlappingBooking(
            @Param("kolId") UUID kolId,
            @Param("newStartAt") Instant newStartAt,
            @Param("newEndAt") Instant newEndAt
    );

    @Query("""
            SELECT br FROM BookingRequest br
            LEFT JOIN FETCH br.attachedFiles fu
            LEFT JOIN FETCH fu.file f
            WHERE br.id = :bookingRequestId AND f.status = 'ACTIVE'
            """)
    BookingRequest findByIdWithAttachedFiles(@Param("bookingRequestId") UUID bookingRequestId);

}
