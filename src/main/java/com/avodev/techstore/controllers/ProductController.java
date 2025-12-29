package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.ProductRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.ProductResponse;
import com.avodev.techstore.services.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        return ApiResponse.<List<ProductResponse>>builder()
                .data(productService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductDetail(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.getProductDetail(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest req) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.createProduct(req))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequest req) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.updateProduct(id, req))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }
    
}
