package com.web.bookingKol.domain.blog.dtos;

import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class BlogDTO {
    private Integer id;
    private String author;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isPublish;
    private String thumbnail;
}
