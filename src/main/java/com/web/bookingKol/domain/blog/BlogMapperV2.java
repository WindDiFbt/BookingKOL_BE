package com.web.bookingKol.domain.blog;

import com.web.bookingKol.domain.blog.dtos.BlogDTO;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class BlogMapperV2 {
    private static final int SUMMARY_MAX_LENGTH = 200;

    public BlogDTO toDtoWithoutContent(Blog blog) {
        BlogDTO blogDTO = new BlogDTO();
        blogDTO.setId(blog.getId());
        blogDTO.setAuthor(blog.getAuthor());
        blogDTO.setTitle(blog.getTitle());
        String htmlContent = blog.getContent();
        String summary;
        if (htmlContent != null && !htmlContent.isEmpty()) {
            String plainText = Jsoup.parse(htmlContent).text();
            if (plainText.length() > SUMMARY_MAX_LENGTH) {
                summary = plainText.substring(0, SUMMARY_MAX_LENGTH) + "...";
            } else {
                summary = plainText;
            }
        } else {
            summary = "";
        }
        blogDTO.setContent(summary);
        blogDTO.setIsPublish(blog.getIsPublish());
        blogDTO.setCreatedAt(blog.getCreatedAt());
        blogDTO.setUpdatedAt(blog.getUpdatedAt());
        blogDTO.setThumbnail(blog.getThumbnail());
        return blogDTO;
    }
}
