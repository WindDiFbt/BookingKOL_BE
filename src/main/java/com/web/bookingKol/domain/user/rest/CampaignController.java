package com.web.bookingKol.domain.user.rest;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.services.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PreAuthorize("hasAnyAuthority('ADMIN','USER','SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCampaignDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.getCampaignDetail(id));
    }
}

