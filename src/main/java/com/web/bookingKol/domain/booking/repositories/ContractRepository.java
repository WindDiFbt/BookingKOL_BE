package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.booking.models.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

    @Query("""
                SELECT c FROM Contract c
                LEFT JOIN c.bookingRequest br
                LEFT JOIN br.campaign camp
                WHERE LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(camp.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(br.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Contract> searchContractsByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT SUM(c.amount) FROM Contract c " +
            "JOIN c.bookingRequest b " +
            "WHERE b.kol.id = :kolId " +
            "AND c.status = :status " +
            "AND b.createdAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(UUID kolId, Instant start, Instant end, String status);

    @Query("SELECT SUM(c.amount) FROM Contract c " +
            "JOIN c.bookingRequest b " +
            "WHERE b.kol.id = :kolId " +
            "AND c.status IN :statuses " +
            "AND b.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueBetween(UUID kolId, Instant start, Instant end, List<String> statuses);

    // Lấy dữ liệu cho biểu đồ doanh thu
    @Query("SELECT CAST(b.endAt AS date) as label, SUM(c.amount) as value " +
            "FROM Contract c " +
            "JOIN c.bookingRequest b " +
            "WHERE b.kol.id = :kolId " +
            "AND b.status = 'COMPLETED' " +
            "AND b.endAt BETWEEN :start AND :end " +
            "GROUP BY CAST(b.endAt AS date) " +
            "ORDER BY label ")
    List<IChartDataPointProjection> getRevenueChartData(UUID kolId, Instant start, Instant end);
}
