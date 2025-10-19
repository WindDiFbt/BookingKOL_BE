package com.web.bookingKol.domain.payment.repositories;

import com.web.bookingKol.domain.payment.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {

    public interface TransactionStatsProjection {
        String getStatus();

        long getCount();

        BigDecimal getTotalAmountIn();
    }

    @Query("SELECT " +
            "  t.status as status, " +
            "  COUNT(t.id) as count, " +
            "  SUM(t.amountIn) as totalAmountIn " +
            "FROM Transaction t " +
            "WHERE (CAST(:startDate AS java.time.Instant) IS NULL OR t.createdAt >= :startDate) " +
            "  AND (CAST(:endDate AS java.time.Instant) IS NULL OR t.createdAt <= :endDate) " +
            "GROUP BY t.status")
    List<TransactionStatsProjection> getTransactionStats(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
