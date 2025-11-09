package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolLeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface KolLeaveRequestRepository extends JpaRepository<KolLeaveRequest, UUID> {
    List<KolLeaveRequest> findByKol_Id(UUID kolId);
    boolean existsByAvailability_IdAndStatus(UUID availabilityId, String status);

    Page<KolLeaveRequest> findByKol_Id(UUID kolId, Pageable pageable);

    @Query("""
        SELECT l FROM KolLeaveRequest l
        WHERE l.kol.id = :kolId
        AND (LOWER(l.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.status) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<KolLeaveRequest> searchByKolAndReason(@Param("kolId") UUID kolId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    @Query("""
    SELECT l FROM KolLeaveRequest l
    WHERE (LOWER(l.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.kol.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<KolLeaveRequest> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT l FROM KolLeaveRequest l
    WHERE (LOWER(l.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.kol.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND LOWER(l.status) = LOWER(:status)
""")
    Page<KolLeaveRequest> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                                   @Param("status") String status,
                                                   Pageable pageable);

    Page<KolLeaveRequest> findByStatusIgnoreCase(String status, Pageable pageable);

}

