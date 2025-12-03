package com.avodev.techstore.requests;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Long shippingAddressId;
    String customerNote;
    String deliveryMethod;
    String paymentMethod;
    List<OrderItemRequest> items;
    Boolean fromCart;
}
