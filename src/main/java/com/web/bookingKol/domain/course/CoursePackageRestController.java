package com.web.bookingKol.domain.course;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.services.CoursePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/courses")
public class CoursePackageRestController {
    @Autowired
    private CoursePackageService coursePackageService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse<?>> getAllCourses() {
        return ResponseEntity.ok().body(coursePackageService.getAllCourse());
    }

    @GetMapping("/{coursePackageId}")
    ResponseEntity<ApiResponse<?>> getCourseById(@PathVariable UUID coursePackageId) {
        return ResponseEntity.ok().body(coursePackageService.getCoursePackageById(coursePackageId));
    }
}
