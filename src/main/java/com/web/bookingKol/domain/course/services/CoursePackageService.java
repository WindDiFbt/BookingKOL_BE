package com.web.bookingKol.domain.course.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.dtos.CoursePackageDTO;
import com.web.bookingKol.domain.course.dtos.UpdateCoursePackageDTO;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface CoursePackageService {
    ApiResponse<CoursePackageDTO> getCoursePackageById(UUID coursePackageId);

    ApiResponse<Page<CoursePackageDTO>> getAllCourse(Integer minPrice,
                                                     Integer maxPrice,
                                                     Integer minDiscount,
                                                     Integer maxDiscount,
                                                     int page,
                                                     int size,
                                                     String sortBy,
                                                     String sortDir);

    ApiResponse<Page<CoursePackageDTO>> getAllCoursesAdmin(Boolean isAvailable,
                                                           Integer minPrice,
                                                           Integer maxPrice,
                                                           Integer minDiscount,
                                                           Integer maxDiscount,
                                                           int page,
                                                           int size,
                                                           String sortBy,
                                                           String sortDir);

    ApiResponse<CoursePackageDTO> getDetailCourseAdmin(UUID coursePackageId);

    ApiResponse<CoursePackageDTO> createCoursePackage(UUID adminId, CoursePackageDTO coursePackageDTO, List<MultipartFile> courseMedias);

    ApiResponse<CoursePackageDTO> updateCoursePackage(UUID coursePackageId, UpdateCoursePackageDTO updateCoursePackageDTO);

    ApiResponse<List<FileUsageDTO>> uploadCourseMediaFiles(UUID uploaderId, UUID courseId, List<MultipartFile> files);

    ApiResponse<?> removeCourseMediaFile(UUID courseId, List<UUID> fileUsageIds);

    ApiResponse<FileUsageDTO> setCoverImage(UUID courseId, UUID fileId);

    ApiResponse<?> deleteCoursePackage(UUID courseId);
}
