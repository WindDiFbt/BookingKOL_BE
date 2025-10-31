package com.web.bookingKol.domain.blog.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogCreateDTO {
    private String author;
    @NotNull(message = "Title is required")
    private String title;
    @NotNull(message = "Content is required")
    private String content;
    private Boolean isPublish;
}
