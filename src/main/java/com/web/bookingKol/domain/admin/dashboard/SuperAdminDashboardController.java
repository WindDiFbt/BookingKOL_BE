package com.web.bookingKol.domain.admin.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/superadmin/dashboard")
public class SuperAdminDashboardController {
    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<?> getAdminSummary(@RequestParam(required = false) Instant startDate,
                                             @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(adminDashboardService.getAdminDashboard(startDate, endDate));
    }

}
