package com.web.bookingKol.domain.admin;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.kol.dtos.NewKolDTO;
import com.web.bookingKol.domain.kol.dtos.UpdateKolDTO;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("admin/kol")
public class AdminKolRestController {
    @Autowired
    private KolProfileService kolProfileService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllKolProfile(@RequestParam(required = false) BigDecimal minBookingPrice,
                                              @RequestParam(required = false) Boolean isAvailable,
                                              @RequestParam(required = false) Double minRating,
                                              @RequestParam(required = false) Enums.Roles role,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kolProfileService.getAllKol(minBookingPrice, isAvailable, minRating, page, size, role));
    }

    @PostMapping("/create-new-kol")
    public ResponseEntity<?> createNewKolAccount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestPart(value = "fileAvatar", required = false) MultipartFile fileAvatar,
                                                 @RequestPart @Valid NewKolDTO newKolDTO) {
        UUID AdminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kolProfileService.createNewKolAccount(AdminId, newKolDTO, fileAvatar));
    }

    @PutMapping("/update/{kolId}")
    public ResponseEntity<?> updateKolProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable UUID kolId,
                                              @RequestBody UpdateKolDTO updateKolDTO) {
        UUID AdminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.updateKolProfile(AdminId, kolId, updateKolDTO));
    }

    @PostMapping("/medias/upload/{kolId}")
    public ResponseEntity<?> uploadKolImagePortfolio(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable UUID kolId,
                                                     @RequestParam("files") List<MultipartFile> files) {
        UUID AdminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kolProfileService.uploadKolMedias(AdminId, kolId, files));
    }

    @GetMapping("/medias/all/{kolId}")
    public ResponseEntity<?> getAllKol(@PathVariable UUID kolId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.getAllKolMediaFiles(kolId));
    }

    @PutMapping("/medias/activate/{kolId}")
    public ResponseEntity<?> activateKolMediaFile(@PathVariable UUID kolId,
                                                  @RequestParam List<UUID> fileUsageIds) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.activateOrDeactivateKolMediaFile(kolId, fileUsageIds, true));
    }

    @PutMapping("/medias/deactivate/{kolId}")
    public ResponseEntity<?> deactivateKolMediaFile(@PathVariable UUID kolId,
                                                    @RequestParam List<UUID> fileUsageIds) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.activateOrDeactivateKolMediaFile(kolId, fileUsageIds, false));
    }

    @PutMapping("/cover-image/{kolId}")
    public ResponseEntity<?> setCoverImage(@PathVariable UUID kolId,
                                           @RequestParam UUID fileId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setCoverImage(kolId, fileId));
    }

    @PutMapping("/avatar/change/existed-image/{kolId}")
    public ResponseEntity<?> setAvatarKolWithExistedFile(@PathVariable UUID kolId,
                                                         @RequestParam UUID fileId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setAvatarWithExistedImage(kolId, fileId));
    }

    @PostMapping("/avatar/change/new-image/{kolId}")
    public ResponseEntity<?> setAvatarKolWithUploadNewImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @PathVariable UUID kolId,
                                                            @RequestParam("fileAvatar") MultipartFile fileAvatar) {
        UUID adminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setAvatarWithUploadNewImage(adminId, kolId, fileAvatar));
    }

    @PostMapping("/category/add/{kolId}")
    public ResponseEntity<?> addCategoryForKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable UUID kolId,
                                               @RequestParam UUID categoryId) {
        UUID adminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.addCategoryForKol(adminId, kolId, categoryId));
    }

    @DeleteMapping("/category/remove/{kolId}")
    public ResponseEntity<?> removeCategoryForKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable UUID kolId,
                                                  @RequestParam UUID categoryId) {
        UUID adminId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.removeCategoryForKol(adminId, kolId, categoryId));
    }

    @PatchMapping("/medias/delete/{fileId}")
    public ResponseEntity<?> deleteFileMedia(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable UUID fileId) {
        UUID changerId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.deleteFileMedia(changerId, fileId));
    }
}
