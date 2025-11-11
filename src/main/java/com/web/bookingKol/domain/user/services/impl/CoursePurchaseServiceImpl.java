package com.web.bookingKol.domain.user.services.impl;


import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.user.dtos.PurchasedCourseResponse;
import com.web.bookingKol.domain.user.mappers.UserMapper;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.CoursePurchaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoursePurchaseServiceImpl implements CoursePurchaseService {
    private final UserRepository userRepository;
    private final PurchasedCoursePackageRepository purchasedCoursePackageRepository;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<PagedResponse<PurchasedCourseResponse>> getPurchaseHistoryUser(
            UUID userId, String search, Instant startDate, Instant endDate, Pageable pageable) {
        User user = userRepository.findById(userId)
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
                .email(p.getEmail())
                .phoneNumber(p.getPhoneNumber())
                .purchasedCourseNumber(p.getPurchasedCourseNumber())
                .build());
        return ApiResponse.<PagedResponse<PurchasedCourseResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch sử mua khóa học thành công"))
                .data(PagedResponse.fromPage(response))
                .build();
    }

    @Override
    public ApiResponse<PagedResponse<PurchasedCourseResponse>> getPurchaseHistoryAdmin(String search, Instant startDate, Instant endDate, Pageable pageable) {
        Specification<PurchasedCoursePackage> spec = (root, query, cb) -> cb.conjunction();
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
                .email(p.getEmail())
                .phoneNumber(p.getPhoneNumber())
                .purchasedCourseNumber(p.getPurchasedCourseNumber())
                .user(userMapper.toDto(p.getUser()))
                .build());
        return ApiResponse.<PagedResponse<PurchasedCourseResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch sử mua khóa học thành công"))
                .data(PagedResponse.fromPage(response))
                .build();
    }

    @Override
    public ApiResponse<?> confirmPurchasedCourse(UUID purchasedCourseId) {
        PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageRepository.findById(purchasedCourseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch sử mua khóa học với ID: " + purchasedCourseId));
        if (purchasedCoursePackage.getStatus().equals(Enums.PurchasedCourse.COURSEASSIGNED.name())) {
            throw new IllegalArgumentException("Khóa học đã được xác nhận");
        }
        purchasedCoursePackage.setStatus(Enums.PurchasedCourse.COURSEASSIGNED.name());
        purchasedCoursePackageRepository.save(purchasedCoursePackage);
        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xác nhận khóa học thành công"))
                .data(null)
                .build();
    }
}


