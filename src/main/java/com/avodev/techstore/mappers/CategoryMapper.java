package com.avodev.techstore.mappers;

import com.avodev.techstore.entities.Category;
import com.avodev.techstore.responses.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
