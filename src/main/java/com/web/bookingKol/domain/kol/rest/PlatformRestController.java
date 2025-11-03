package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.kol.dtos.PlatformDTO;
import com.web.bookingKol.domain.kol.mappers.PlatformMapper;
import com.web.bookingKol.domain.kol.repositories.PlatformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/platforms")
@PreAuthorize("permitAll()")
public class PlatformRestController {
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private PlatformMapper platformMapper;

    @GetMapping("/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllPlatform() {
        List<PlatformDTO> platformDTOS = platformRepository.findAll().stream().map(platformMapper::toDto).toList();
        return ResponseEntity.ok(platformDTOS);
    }
}
