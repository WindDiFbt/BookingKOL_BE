package com.web.bookingKol.domain.course.mappers;

import com.web.bookingKol.domain.course.dtos.CoursePaymentDTO;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import org.springframework.stereotype.Component;

@Component
public class CoursePaymentMapper {
    public CoursePaymentDTO toDto(PurchasedCoursePackage purchasedCoursePackage) {
        CoursePaymentDTO dto = new CoursePaymentDTO();
        dto.setId(purchasedCoursePackage.getId());
        dto.setUserId(purchasedCoursePackage.getUser().getId());
        dto.setCoursePackageId(purchasedCoursePackage.getCoursePackage().getId());
        dto.setPrice(purchasedCoursePackage.getCoursePackage().getPrice());
        dto.setDiscount(purchasedCoursePackage.getCoursePackage().getDiscount());
        dto.setCurrentPrice(purchasedCoursePackage.getCurrentPrice());
        dto.setEmail(purchasedCoursePackage.getEmail());
        dto.setStartDate(purchasedCoursePackage.getStartDate());
        dto.setEndDate(purchasedCoursePackage.getEndDate());
        dto.setPurchasedCourseNumber(purchasedCoursePackage.getPurchasedCourseNumber());
        return dto;
    }
}
