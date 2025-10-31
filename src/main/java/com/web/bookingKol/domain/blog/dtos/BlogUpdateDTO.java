package com.web.bookingKol.domain.blog.dtos;

import lombok.Data;

@Data
public class BlogUpdateDTO {
    private String author;
    private String title;
    private String content;
    private Boolean isPublish;
}
