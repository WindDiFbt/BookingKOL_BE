package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.CategoryDTO;
import com.web.bookingKol.domain.kol.models.Category;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDto(Category category);

    List<CategoryDTO> toDtoList(List<Category> categories);

    Set<CategoryDTO> toDtoSet(Set<Category> categories);
}
