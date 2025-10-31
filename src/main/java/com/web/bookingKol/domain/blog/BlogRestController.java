package com.web.bookingKol.domain.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blogs")
@PreAuthorize("permitAll()")
public class BlogRestController {
    @Autowired
    private BlogService blogService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllBlog(Pageable pageable) {
        return ResponseEntity.ok().body(blogService.getAllBlog(pageable, false));
    }

    @GetMapping("/detail/{blogId}")
    public ResponseEntity<?> getDetailBlog(@PathVariable Integer blogId) {
        return ResponseEntity.ok().body(blogService.getDetailBlog(blogId, false));
    }
}
