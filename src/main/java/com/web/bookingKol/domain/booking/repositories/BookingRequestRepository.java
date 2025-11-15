package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.admin.dashboard.KolBookingCountDTO;
import com.web.bookingKol.domain.admin.dashboard.PlatformCountDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
            SELECT DISTINCT br FROM BookingRequest br
            LEFT JOIN FETCH br.attachedFiles fu
            LEFT JOIN FETCH fu.file f
            WHERE br.id = :bookingRequestId
            """)
    BookingRequest findByIdWithAttachedFiles(@Param("bookingRequestId") UUID bookingRequestId);

    @Query("SELECT CASE WHEN COUNT(br) > 0 THEN TRUE ELSE FALSE END FROM BookingRequest br WHERE br.requestNumber = :requestNumber")
    boolean existsByRequestNumber(@Param("requestNumber") String requestNumber);

    Page<BookingRequest> findByUser_Id(UUID userId, Pageable pageable);

    Page<BookingRequest> findByUser_IdAndCampaignIsNotNull(UUID userId, Pageable pageable);


    Page<BookingRequest> findByCampaignIsNotNull(Pageable pageable);

    @Query("""
            SELECT br FROM BookingRequest br
            WHERE br.user.id = :userId
              AND br.campaign IS NOT NULL
              AND (
                  LOWER(br.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(br.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR EXISTS (
                      SELECT c FROM Contract c
                      WHERE c.bookingRequest.id = br.id
                        AND LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
              )
            """)
    Page<BookingRequest> searchByUserAndKeyword(@Param("userId") UUID userId,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);

    @Query("SELECT DISTINCT b FROM BookingRequest b " +
            "LEFT JOIN FETCH b.contracts c " +
            "WHERE b.startAt BETWEEN :start AND :end AND b.endAt BETWEEN :start AND :end AND" +
            " b.status IN :statuses")
    List<BookingRequest> findAllByKolIdAndStartAtBetweenAndStatusIn(
            Instant start, Instant end, List<String> statuses);

    @Query("""
            SELECT k FROM BookingRequest k
            WHERE k.status = :status AND k.startAt BETWEEN :start AND :end
            """)
    List<BookingRequest> findAllByKolIdAndStatusInAndStartAtAfter(
            String status, Instant start, Instant end, Sort sort);

    @Query("SELECT " +
            "  k.id AS kolId, " +
            "  k.user.fullName AS kolName, " +
            "  COUNT(b.id) AS bookingCount " +
            "FROM BookingRequest b " +
            "JOIN b.kol k " +
            "GROUP BY k.id, k.user.fullName " +
            "ORDER BY bookingCount DESC")
    List<KolBookingCountDTO> findKolBookingCounts();

    @Query("SELECT " +
            "  k.id AS kolId, " +
            "  k.user.fullName AS kolName, " +
            "  COUNT(b.id) AS bookingCount " +
            "FROM BookingRequest b " +
            "JOIN b.kol k " +
            "WHERE b.status IN :statuses " +
            "AND b.startAt BETWEEN :start AND :end AND b.endAt BETWEEN :start AND :end " +
            "GROUP BY k.id, k.user.fullName " +
            "ORDER BY bookingCount DESC")
    List<KolBookingCountDTO> findTopKolBookingCounts(Pageable pageable, List<String> statuses, Instant start, Instant end);

    @Query("SELECT " +
            "  b.platform AS platform, " +
            "  COUNT(b.id) AS count " +
            "FROM BookingRequest b " +
            "WHERE b.platform IS NOT NULL " +
            "AND b.startAt BETWEEN :start AND :end AND b.endAt BETWEEN :start AND :end " +
            "AND b.status IN :statuses " +
            "GROUP BY b.platform")
    List<PlatformCountDTO> findPlatformCounts(List<String> statuses, Instant start, Instant end);

    boolean existsByCampaign_Id(UUID campaignId);

    List<BookingRequest> findByCampaign_Id(UUID campaignId);

    Optional<BookingRequest> findFirstByCampaign_Id(UUID campaignId);

}
