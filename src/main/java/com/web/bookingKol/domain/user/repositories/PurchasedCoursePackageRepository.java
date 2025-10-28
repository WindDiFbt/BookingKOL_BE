package com.web.bookingKol.domain.user.repositories;

import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurchasedCoursePackageRepository extends JpaRepository<PurchasedCoursePackage, UUID>,
        JpaSpecificationExecutor<PurchasedCoursePackage> {

    @Query("SELECT COUNT(p) > 0 FROM PurchasedCoursePackage p WHERE p.coursePackage.id = :coursePackageId")
    boolean existsPurchasedCoursePackageByCoursePackageId(@Param("coursePackageId") UUID coursePackageId);

    @Query("SELECT COUNT(p) > 0 FROM PurchasedCoursePackage p WHERE p.purchasedCourseNumber = :purchasedCourseNumber")
    boolean existsByPurchasedCourseNumber(@Param("purchasedCourseNumber") String purchasedCourseNumber);

    @Query("SELECT p FROM PurchasedCoursePackage p WHERE p.payment.id = :paymentId")
    PurchasedCoursePackage findPurchasedCoursePackageByPaymentId(@Param("paymentId") UUID paymentId);
}


