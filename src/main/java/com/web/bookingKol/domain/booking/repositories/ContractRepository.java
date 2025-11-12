package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.admin.dashboard.BookingMonthlyTrendDTO;
import com.web.bookingKol.domain.admin.dashboard.KolBookingRevenueDTO;
import com.web.bookingKol.domain.booking.dtos.BookingExportDTO;
import com.web.bookingKol.domain.booking.models.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
            "WHERE c.status = :status " +
            "AND b.startAt BETWEEN :start AND :end AND b.endAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(Instant start, Instant end, String status);

    @Query("SELECT SUM(c.amount) FROM Contract c " +
            "JOIN c.bookingRequest b " +
            "WHERE c.status IN :statuses " +
            "AND b.startAt BETWEEN :start AND :end AND b.endAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueBetween(Instant start, Instant end, List<String> statuses);

    @Query("SELECT CAST(b.endAt AS date) as label, SUM(c.amount) as value " +
            "FROM Contract c " +
            "JOIN c.bookingRequest b " +
            "WHERE b.status = 'COMPLETED' " +
            "AND b.endAt BETWEEN :start AND :end " +
            "GROUP BY CAST(b.endAt AS date) " +
            "ORDER BY label ")
    List<IChartDataPointProjection> getRevenueChartData(Instant start, Instant end);

    @Query("SELECT b.status as label, COUNT(b) as value " +
            "FROM Contract b " +
            "WHERE b.createdAt >= :start AND b.createdAt <= :end " +
            "AND b.status NOT IN :statuses " +
            "GROUP BY b.status")
    List<IChartDataPointProjection> getStatusBreakdown(Instant start, Instant end, List<String> statuses);

    @Query("SELECT " +
            "  YEAR(b.createdAt) AS year, " +   // <-- SỬA Ở ĐÂY
            "  MONTH(b.createdAt) AS month, " + // <-- SỬA Ở ĐÂY
            "  COUNT(b.id) AS count " +
            "FROM Contract b " +
            "WHERE b.createdAt >= :startDate " +
            "AND b.status IN :statuses " +
            "GROUP BY YEAR(b.createdAt), MONTH(b.createdAt) " + // <-- SỬA Ở ĐÂY
            "ORDER BY year, month ASC")
    List<BookingMonthlyTrendDTO> findBookingMonthlyTrend(@Param("startDate") Instant startDate, List<String> statuses);

    @Query("SELECT " +
            "  k.id AS kolId, " +
            "  k.user.fullName AS kolName, " +
            "  SUM(c.amount) AS totalRevenue " +
            "FROM Contract c " +
            "JOIN c.bookingRequest.kol k " +
            "WHERE c.amount IS NOT NULL " + // Chỉ tính các booking có doanh thu
            "AND c.bookingRequest.status IN :statuses " +
            "AND c.bookingRequest.startAt BETWEEN :start AND :end AND c.bookingRequest.endAt BETWEEN :start AND :end " +
            "GROUP BY k.id, k.user.fullName " +
            "ORDER BY totalRevenue DESC")
    List<KolBookingRevenueDTO> findTopKolBookingRevenue(Pageable pageable, List<String> statuses, Instant start, Instant end);

    @Query("SELECT c FROM Contract c WHERE (:bookingType IS NULL OR c.bookingRequest.bookingType = :bookingType)")
    List<Contract> findAllByBookingType(@Param("bookingType") String bookingType);

    @Query("SELECT new com.web.bookingKol.domain.booking.dtos.BookingExportDTO(" +
            "br.id, c.contractNumber, c.status, c.amount, " +
            "br.bookingNumber, br.requestNumber, br.status, " +
            "COALESCE(k.displayName, ku.fullName), " + // Logic lấy tên KOL
            "bu.fullName, bu.email, bu.phone, " +
            "camp.name, " +
            "br.bookingType, br.platform, " +
            "br.startAt, br.endAt, " +
            "c.signedAtBrand, c.signedAtKol) " +
            "FROM Contract c " +
            "LEFT JOIN c.bookingRequest br " + // JOIN bình thường
            "LEFT JOIN br.kol k " +
            "LEFT JOIN k.user ku " +
            "LEFT JOIN br.user bu " +
            "LEFT JOIN br.campaign camp " +
            "WHERE (:bookingType IS NULL OR c.bookingRequest.bookingType = :bookingType)")
    List<BookingExportDTO> findAllForExport(@Param("bookingType") String bookingType);

    @EntityGraph(attributePaths = {
            "bookingRequest",
            "bookingRequest.kol",
            "bookingRequest.kol.user",
            "bookingRequest.user",
            "bookingRequest.campaign"
    })
    @Query("SELECT c FROM Contract c WHERE (:bookingType IS NULL OR c.bookingRequest.bookingType = :bookingType)")
    List<Contract> findAllWithGraph(@Param("bookingType") String bookingType);

}
