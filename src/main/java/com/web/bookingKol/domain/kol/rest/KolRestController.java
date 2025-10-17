package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.kol.dtos.UpdateKolDTO;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kol")
public class KolRestController {
    @Autowired
    private KolProfileService kolProfileService;

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody UpdateKolDTO updateKolDTO) {
        UUID changerId = userDetails.getId();
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.ok(kolProfileService.updateKolProfile(changerId, kolId, updateKolDTO));
    }

    @PatchMapping("/medias/delete/{fileId}")
    public ResponseEntity<?> deleteFileMedia(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable UUID fileId) {
        UUID changerId = userDetails.getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.deleteFileMedia(changerId, fileId));
    }

    @PostMapping("/medias/upload")
    public ResponseEntity<?> uploadKolImagePortfolio(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestParam("files") List<MultipartFile> files) {
        UUID uploaderId = userDetails.getId();
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kolProfileService.uploadKolMedias(uploaderId, kolId, files));
    }

    @GetMapping("/medias/all")
    public ResponseEntity<?> getAllKol(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.getAllKolMediaFiles(kolId));
    }

    @PutMapping("/medias/activate")
    public ResponseEntity<?> activateKolMediaFile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestParam List<UUID> fileUsageIds) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.activateOrDeactivateKolMediaFile(kolId, fileUsageIds, true));
    }

    @PutMapping("/medias/deactivate")
    public ResponseEntity<?> deactivateKolMediaFile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestParam List<UUID> fileUsageIds) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.activateOrDeactivateKolMediaFile(kolId, fileUsageIds, false));
    }

    @PutMapping("/cover-image/change")
    public ResponseEntity<?> setCoverImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam UUID fileId) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setCoverImage(kolId, fileId));
    }

    @PutMapping("/avatar/change/existed-image")
    public ResponseEntity<?> setAvatarKolWithExistedFile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @RequestParam UUID fileId) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setAvatarWithExistedImage(kolId, fileId));
    }

    @PostMapping("/avatar/change/new-image")
    public ResponseEntity<?> setAvatarKolWithUploadNewImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @RequestParam("fileAvatar") MultipartFile fileAvatar) {
        UUID changerId = userDetails.getId();
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.setAvatarWithUploadNewImage(changerId, kolId, fileAvatar));
    }

    @PostMapping("/category/add")
    public ResponseEntity<?> addCategoryForKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestParam UUID categoryId) {
        UUID changerId = userDetails.getId();
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.addCategoryForKol(changerId, kolId, categoryId));
    }

    @DeleteMapping("/category/remove")
    public ResponseEntity<?> removeCategoryForKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestParam UUID categoryId) {
        UUID changerId = userDetails.getId();
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.status(HttpStatus.OK)
                .body(kolProfileService.removeCategoryForKol(changerId, kolId, categoryId));
    }
}
