package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.ProductVariantRequest;
import com.avodev.techstore.requests.SearchRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.PageableResponse;
import com.avodev.techstore.responses.ProductVariantResponse;
import com.avodev.techstore.services.ProductVariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("product-variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductVariantController {
    ProductVariantService productVariantService;

    @PostMapping("/search")
    ApiResponse<PageableResponse<ProductVariantResponse>> searchProductVariants(@RequestBody SearchRequest<ProductVariantRequest> request) {
        return ApiResponse.<PageableResponse<ProductVariantResponse>>builder()
                .data(productVariantService.searchProductVariants(request))
                .build();
    }
}
