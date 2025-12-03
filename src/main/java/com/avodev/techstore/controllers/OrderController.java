package com.avodev.techstore.controllers;


import com.avodev.techstore.requests.OrderRequest;
import com.avodev.techstore.responses.ApiResponse;
import com.avodev.techstore.responses.OrderResponse;
import com.avodev.techstore.services.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping("/preview")
    public ApiResponse<OrderResponse> previewOrder(@RequestBody OrderRequest orderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.previewOrder(orderRequest))
                .build();
    }

    @PostMapping("/place")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.placeOrder(orderRequest))
                .build();
    }

}
