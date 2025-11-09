package com.web.bookingKol.domain.blog;

import com.web.bookingKol.domain.blog.dtos.BlogDTO;
import org.springframework.stereotype.Component;

@Component
public class BlogMapperV2 {
    public BlogDTO toDtoWithoutContent(Blog blog) {
        BlogDTO blogDTO = new BlogDTO();
        blogDTO.setId(blog.getId());
        blogDTO.setAuthor(blog.getAuthor());
        blogDTO.setTitle(blog.getTitle());
        blogDTO.setContent(blog.getContent().substring(0, Math.min(blog.getContent().length(), 200)));
        blogDTO.setIsPublish(blog.getIsPublish());
        blogDTO.setCreatedAt(blog.getCreatedAt());
        blogDTO.setUpdatedAt(blog.getUpdatedAt());
        return blogDTO;
    }
}
