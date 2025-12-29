package com.avodev.techstore.controllers;

import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.ProductVariantCardResponse;
import com.avodev.techstore.services.WishlistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WishlistController {
    WishlistService wishlistService;

    @GetMapping
    public ApiResponse<List<ProductVariantCardResponse>> getWishlistItems() {
        return ApiResponse.<List<ProductVariantCardResponse>>builder()
                .data(wishlistService.getWishlistItems())
                .build();
    }

    @PostMapping("/add/{variantId}")
    public ApiResponse<Void> addItem(@PathVariable Long variantId) {
        wishlistService.addItem(variantId);
        return ApiResponse.<Void>builder()
                .message("Thêm sản phẩm yêu thích thành công")
                .build();
    }

    @DeleteMapping("/remove")
    public ApiResponse<Void> removeItems(@RequestBody List<Long> variantIds) {
        wishlistService.removeItems(variantIds);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm yêu thích thành công")
                .build();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearWishlist() {
        wishlistService.clearWishlist();
        return ApiResponse.<Void>builder()
                .message("Xóa danh sách yêu thích thành công")
                .build();
    }

    @GetMapping("/count")
    public ApiResponse<Integer> getItemCount() {
        return ApiResponse.<Integer>builder()
                .data(wishlistService.getItemCount())
                .build();
    }
}
