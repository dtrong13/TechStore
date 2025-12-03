package com.avodev.techstore.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    AddressResponse deliveryAddress;
    String customerNote;
    String deliveryMethod;
    String paymentMethod;
    String status;
    LocalDateTime orderDate;
    Long subtotal;
    Long shippingFee;
    Long totalMoney;
    String trackingNumber;
    List<OrderItemResponse> items;
}
