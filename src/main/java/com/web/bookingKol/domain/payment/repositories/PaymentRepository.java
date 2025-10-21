package com.web.bookingKol.domain.payment.repositories;

import com.web.bookingKol.domain.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.expiresAt <= :currentTime")
    List<Payment> findPendingAndExpired(@Param("currentTime") Instant currentTime);

    @Query("SELECT p FROM Payment p WHERE p.contract.id = :contractId")
    Payment findByContractId(UUID contractId);
}
