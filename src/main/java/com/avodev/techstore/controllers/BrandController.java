package com.avodev.techstore.controllers;

import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.BrandResponse;
import com.avodev.techstore.services.BrandService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandController {
    BrandService brandService;

    @GetMapping("/by-category/{categoryId}")
    public ApiResponse<List<BrandResponse>> getBrandsByCategory(@PathVariable Long categoryId) {
        List<BrandResponse> result = brandService.getBrandsByCategory(categoryId);
        return ApiResponse.<List<BrandResponse>>builder()
                .data(result)
                .build();
    }


}
