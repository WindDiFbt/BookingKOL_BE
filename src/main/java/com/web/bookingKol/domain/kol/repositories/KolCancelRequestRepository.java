package com.web.bookingKol.domain.kol.repositories;


import com.web.bookingKol.domain.kol.models.KolCancelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface KolCancelRequestRepository extends JpaRepository<KolCancelRequest, UUID> {
    boolean existsByWorkTime_IdAndStatus(UUID workTimeId, String status);
    Optional<KolCancelRequest> findByWorkTime_Id(UUID workTimeId);
}

