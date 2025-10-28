package com.web.bookingKol.domain.user.services.impl;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.PurchasedCourseResponse;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.CoursePurchaseService;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursePurchaseServiceImpl implements CoursePurchaseService {

    private final UserRepository userRepository;
    private final PurchasedCoursePackageRepository purchasedCoursePackageRepository;

    @Override
    public ApiResponse<PagedResponse<PurchasedCourseResponse>> getPurchaseHistory(
            String userEmail, String search, Instant startDate, Instant endDate, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Specification<PurchasedCoursePackage> spec = (root, query, cb) -> cb.equal(root.get("user"), user);

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("coursePackage").get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("status")), "%" + search.toLowerCase() + "%")
                    ));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), endDate));
        }

        Page<PurchasedCoursePackage> pageResult = purchasedCoursePackageRepository.findAll(spec, pageable);

        Page<PurchasedCourseResponse> response = pageResult.map(p -> PurchasedCourseResponse.builder()
                .id(p.getId())
                .courseName(p.getCoursePackage().getName())
                .currentPrice(p.getCurrentPrice())
                .isPaid(p.getIsPaid())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .build());

        return ApiResponse.<PagedResponse<PurchasedCourseResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch sử mua khóa học thành công"))
                .data(PagedResponse.fromPage(response))
                .build();
    }


}


