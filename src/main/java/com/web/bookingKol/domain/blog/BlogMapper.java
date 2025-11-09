package com.web.bookingKol.domain.blog;

import com.web.bookingKol.domain.blog.dtos.BlogDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    BlogDTO toDto(Blog blog);

    @Mapping(target = "content", ignore = true)
    BlogDTO toDtoWithoutContent(Blog blog);
}
