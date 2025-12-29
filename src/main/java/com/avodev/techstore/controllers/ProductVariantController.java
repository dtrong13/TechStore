package com.avodev.techstore.controllers;

import com.avodev.techstore.requests.ProductVariantRequest;
import com.avodev.techstore.requests.ProductVariantSearchRequest;
import com.avodev.techstore.requests.SearchRequest;
import com.avodev.techstore.responses.*;
import com.avodev.techstore.services.ProductVariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductVariantController {
    ProductVariantService productVariantService;

    @PostMapping("/search")
    ApiResponse<PageableResponse<ProductVariantCardResponse>> searchProductVariants(@RequestBody SearchRequest<ProductVariantSearchRequest> request) {
        return ApiResponse.<PageableResponse<ProductVariantCardResponse>>builder()
                .data(productVariantService.searchProductVariants(request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductVariantDetailResponse> getVariantDetail(@PathVariable("id") Long id) {
        return ApiResponse.<ProductVariantDetailResponse>builder()
                .data(productVariantService.getVariantDetail(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductVariantResponse> create(@RequestBody ProductVariantRequest req) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.createVariant(req))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductVariantResponse> update(@PathVariable Long id, @RequestBody ProductVariantRequest req) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.updateVariant(id, req))
                .build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        productVariantService.updateStock(id, stock);
        return ApiResponse.<Void>builder()
                .message("Stock updated")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productVariantService.deleteVariant(id);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }
}
