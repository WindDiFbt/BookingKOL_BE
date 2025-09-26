package com.web.bookingKol.domain.course.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.CoursePackageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CoursePackageService {
    ApiResponse<CoursePackageDTO> getCoursePackageById(UUID coursePackageId);

    ApiResponse<List<CoursePackageDTO>> getAllCourse();
}
