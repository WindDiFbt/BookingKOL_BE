package com.web.bookingKol.domain.course.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.CoursePackageDTO;
import com.web.bookingKol.domain.course.CoursePackageMapper;
import com.web.bookingKol.domain.course.CoursePackage;
import com.web.bookingKol.domain.course.CoursePackageRepository;
import com.web.bookingKol.domain.course.services.CoursePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CoursePackageServiceImpl implements CoursePackageService {
    @Autowired
    private CoursePackageRepository coursePackageRepository;
    @Autowired
    private CoursePackageMapper coursePackageMapper;

    @Override
    public ApiResponse<CoursePackageDTO> getCoursePackageById(UUID coursePackageId) {
        CoursePackage coursePackage = coursePackageRepository.findByCoursePackageId(coursePackageId, Enums.TargetType.COURSE_PACKAGE.name())
                .orElseThrow(() -> new RuntimeException("CoursePackage not found for id: " + coursePackageId));
        return ApiResponse.<CoursePackageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get course package by coursePackageId success"))
                .data(coursePackageMapper.toDto(coursePackage))
                .build();
    }

    @Override
    public ApiResponse<List<CoursePackageDTO>> getAllCourse() {
        List<CoursePackageDTO> coursePackages = coursePackageMapper.toDtoList(coursePackageRepository.findAll());
        return ApiResponse.<List<CoursePackageDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all course packages success"))
                .data(coursePackages)
                .build();
    }
}
