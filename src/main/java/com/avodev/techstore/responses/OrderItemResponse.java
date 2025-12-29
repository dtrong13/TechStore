package com.avodev.techstore.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    ProductVariantCardResponse productVariant;
    Integer quantity;
    Long totalItemPrice;
}
