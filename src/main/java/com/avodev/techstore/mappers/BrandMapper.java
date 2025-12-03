package com.avodev.techstore.mappers;

import com.avodev.techstore.entities.Brand;
import com.avodev.techstore.responses.BrandResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toBrandResponse(Brand brand);
}
