package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.blog.BlogService;
import com.web.bookingKol.domain.blog.dtos.BlogCreateDTO;
import com.web.bookingKol.domain.blog.dtos.BlogUpdateDTO;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/blogs")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminBlogRestController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private FileService fileService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllBlog(Pageable pageable) {
        return ResponseEntity.ok().body(blogService.getAllBlog(pageable, true));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBlog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody @Valid BlogCreateDTO blogCreateDTO) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(blogService.createBlog(userId, blogCreateDTO));
    }

    @GetMapping("/detail/{blogId}")
    public ResponseEntity<?> getDetailBlog(@PathVariable Integer blogId) {
        return ResponseEntity.ok().body(blogService.getDetailBlog(blogId, true));
    }

    @PatchMapping("/update/{blogId}")
    public ResponseEntity<?> updateBlog(@PathVariable Integer blogId,
                                        @RequestBody BlogUpdateDTO blogUpdateDTO) {
        return ResponseEntity.ok().body(blogService.updateBlog(blogId, blogUpdateDTO));
    }

    @DeleteMapping("/delete/{blogId}")
    public ResponseEntity<?> deleteBlog(@PathVariable Integer blogId) {
        return ResponseEntity.ok().body(blogService.deleteBlog(blogId));
    }

    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(fileService.uploadOneFile(userId, file));
    }

    @PostMapping("/thumbnail/upload/{blogId}")
    public ResponseEntity<?> addThumbnailForBlog(@RequestParam("thumbnail") MultipartFile thumbnail,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @PathVariable Integer blogId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(blogService.addThumbnailForBlog(userId, blogId, thumbnail));
    }

    @DeleteMapping("/thumbnail/delete/{blogId}")
    public ResponseEntity<?> deleteThumbnailForBlog(@PathVariable Integer blogId) {
        return ResponseEntity.ok().body(blogService.deleteThumbnailForBlog(blogId));
    }
}
