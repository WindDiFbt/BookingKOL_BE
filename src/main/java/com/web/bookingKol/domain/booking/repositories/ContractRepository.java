package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.booking.models.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    @Query("SELECT c FROM Contract c WHERE c.bookingRequest.id = :requestId")
    Contract findByRequestId(@Param("requestId") UUID requestId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contract c WHERE c.contractNumber = :contractNumber")
    boolean existsByContractNumber(String contractNumber);

    @Query("""
            SELECT DISTINCT c FROM Contract c
            LEFT JOIN FETCH c.bookingRequest br
            LEFT JOIN FETCH br.kolWorkTimes kwt
            WHERE kwt.id = :workTimeId
            """)
    Contract findByWorkTimeId(@Param("workTimeId") UUID workTimeId);
}
