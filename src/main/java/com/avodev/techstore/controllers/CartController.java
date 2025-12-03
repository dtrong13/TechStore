package com.avodev.techstore.controllers;


import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.CartItemResponse;
import com.avodev.techstore.services.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCartItems() {
        return ApiResponse.<List<CartItemResponse>>builder()
                .data(cartService.getCartItems())
                .build();
    }

    @PostMapping("/add")
    public ApiResponse<Void> addItem(@RequestParam Long variantId, @RequestParam(required = false) Integer quantity) {
        cartService.addItem(variantId, quantity);
        return ApiResponse.<Void>builder()
                .message("Thêm sản phẩm vào giỏ hàng thành công")
                .build();
    }

    @PostMapping("/update")
    public ApiResponse<Void> updateItemQuantity(@RequestParam Long variantId, @RequestParam(required = false) Integer quantity) {
        cartService.updateItemQuantity(variantId, quantity);
        return ApiResponse.<Void>builder()
                .message("Cập nhật số lượng sản phẩm thành công")
                .build();
    }

    @DeleteMapping("/remove")
    public ApiResponse<Void> removeItems(@RequestBody List<Long> variantIds) {
        cartService.removeItems(variantIds);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                .build();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart() {
        cartService.clearCart();
        return ApiResponse.<Void>builder()
                .message("Xóa giỏ hàng thành công")
                .build();
    }

    @GetMapping("/count")
    public ApiResponse<Integer> getItemCount() {
        return ApiResponse.<Integer>builder()
                .data(cartService.getItemCount())
                .build();
    }


}
