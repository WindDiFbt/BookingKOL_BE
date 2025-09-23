package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.kol.dtos.FilterKolDTO;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("kol-profiles")
public class KolProfileRestController {
    @Autowired
    private KolProfileService kolProfileService;

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<?> getKolProfileByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(kolProfileService.getKolProfileByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllKolProfile() {
        return ResponseEntity.ok(kolProfileService.getAllKolProfiles());
    }

    @GetMapping("/kol-id/{kolId}")
    public ResponseEntity<?> getKolProfileByKolId(@PathVariable UUID kolId) {
        return ResponseEntity.ok(kolProfileService.getKolProfileByKolId(kolId));
    }

    @GetMapping("/all-available")
    public ResponseEntity<?> getAllKolAvailableProfile() {
        return ResponseEntity.ok(kolProfileService.getAllKolAvailableProfiles());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getAllKolProfilesByCategoryId(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(kolProfileService.getAllKolProfilesByCategoryId(categoryId));
    }

    @GetMapping("/filters")
    public ResponseEntity<?> getAllKolWithFilter(@RequestBody FilterKolDTO filterKolDTO) {
        return ResponseEntity.ok(kolProfileService.getAllKolWithFilter(filterKolDTO));
    }
}
