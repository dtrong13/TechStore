package com.avodev.techstore.controllers;


import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.CategoryResponse;
import com.avodev.techstore.services.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @GetMapping("/all")
    public ApiResponse<List<CategoryResponse>> findAllCategories() {
        List<CategoryResponse> result = categoryService.findAllCategories();
        return ApiResponse.<List<CategoryResponse>>builder()
                .data(result)
                .build();
    }

}
