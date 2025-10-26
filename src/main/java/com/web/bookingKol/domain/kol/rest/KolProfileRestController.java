package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @GetMapping("/kol-id/{kolId}")
    public ResponseEntity<?> getKolProfileByKolId(@PathVariable UUID kolId) {
        return ResponseEntity.ok(kolProfileService.getKolProfileByKolId(kolId));
    }

    @GetMapping("/all-available")
    public ResponseEntity<?> getAllKolAvailableProfile(
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) Enums.Roles role,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                kolProfileService.getAllKolAvailable(minRating, categoryId, minPrice, page, size, role, nameKeyword)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getAllKolProfilesByCategoryId(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(kolProfileService.getAllKolProfilesByCategoryId(categoryId));
    }
}
