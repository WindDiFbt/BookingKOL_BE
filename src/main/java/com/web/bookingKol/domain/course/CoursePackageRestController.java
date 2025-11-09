package com.web.bookingKol.domain.course;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.dtos.PurchaseCourseReqDTO;
import com.web.bookingKol.domain.course.services.CoursePackageService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/courses")
public class CoursePackageRestController {
    @Autowired
    private CoursePackageService coursePackageService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse<?>> getAllCourses(@RequestParam(required = false) Integer minPrice,
                                                 @RequestParam(required = false) Integer maxPrice,
                                                 @RequestParam(required = false) Integer minDiscount,
                                                 @RequestParam(required = false) Integer maxDiscount,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "price") String sortBy,
                                                 @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok().body(coursePackageService.getAllCourse(
                minPrice, maxPrice, minDiscount, maxDiscount, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/{coursePackageId}")
    ResponseEntity<ApiResponse<?>> getCourseById(@PathVariable UUID coursePackageId) {
        return ResponseEntity.ok().body(coursePackageService.getCoursePackageById(coursePackageId));
    }

    @PostMapping("/purchase/{coursePackageId}")
    ResponseEntity<ApiResponse<?>> purchaseCoursePackage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable UUID coursePackageId,
                                                         @RequestBody @Valid PurchaseCourseReqDTO purchaseCourseReqDTO) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(coursePackageService.purchaseCoursePackage(userId, coursePackageId, purchaseCourseReqDTO));
    }

    @PostMapping("/purchase/confirm/{purchasedId}")
    ResponseEntity<ApiResponse<?>> confirmPurchaseCoursePackage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                @PathVariable UUID purchasedId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(coursePackageService.confirmPurchaseCoursePackage(userId, purchasedId));
    }

    @PatchMapping("/purchase/cancel/{purchasedId}")
    ResponseEntity<ApiResponse<?>> cancelPurchaseCoursePackage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @PathVariable UUID purchasedId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(coursePackageService.cancelPurchaseCoursePackage(userId, purchasedId));
    }
}
