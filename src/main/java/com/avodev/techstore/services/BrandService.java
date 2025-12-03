package com.avodev.techstore.services;


import com.avodev.techstore.mappers.BrandMapper;
import com.avodev.techstore.repositories.BrandRepository;
import com.avodev.techstore.responses.BrandResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {
    BrandRepository brandRepository;
    BrandMapper brandMapper;

    public List<BrandResponse> getBrandsByCategory(Long categoryId) {
        return brandRepository.findBrandsByCategory(categoryId)
                .stream()
                .map(brandMapper::toBrandResponse)
                .collect(Collectors.toList());
    }
}
