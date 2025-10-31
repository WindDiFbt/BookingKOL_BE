package com.web.bookingKol.domain.payment.repositories;

import com.web.bookingKol.domain.payment.models.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    @Query("""
            SELECT r FROM Refund r
            WHERE (:status IS NULL OR r.status = :status)
            AND (:contractId IS NULL OR r.contract.id = :contractId)
            """)
    Page<Refund> findByStatusAndContractIdDynamic(
            @Param("status") String status,
            @Param("contractId") UUID contractId,
            Pageable pageable);
}
