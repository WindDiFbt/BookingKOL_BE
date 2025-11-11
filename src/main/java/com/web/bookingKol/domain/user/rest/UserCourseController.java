package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.services.CoursePurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("user/course")
public class UserCourseController {
    @Autowired
    private CoursePurchaseService coursePurchaseService;

    @GetMapping("/history/all")
    public ResponseEntity<?> getAllPurchaseHistory(@RequestParam(required = false) String search,
                                                   @RequestParam(required = false) Instant startDate,
                                                   @RequestParam(required = false) Instant endDate,
                                                   @PageableDefault(size = 10, sort = "startDate") Pageable pageable,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(coursePurchaseService.getPurchaseHistoryUser(userId, search, startDate, endDate, pageable));
    }
}
